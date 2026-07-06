'use strict';

const fs = require('fs');
const path = require('path');
const { sbFetch } = require('./supabase');

const DEFAULT_SETTING = { deadline_hours: 96, exclude_label: 'No Review' };

/** review_setting 단일 행 조회 — 없거나 실패하면 기본값(4일, 'No Review') */
async function loadSetting(core) {
  try {
    const rows = await sbFetch(
      'review_setting?id=eq.1&select=deadline_hours,exclude_label',
    );
    if (rows.length > 0) return rows[0];
  } catch (error) {
    core.warning(`review_setting 조회 실패, 기본값 사용: ${error.message}`);
  }
  return DEFAULT_SETTING;
}

/** 96 → "4일", 30 → "30시간" */
function formatDeadline(hours) {
  return hours % 24 === 0 ? `${hours / 24}일` : `${hours}시간`;
}

/** 배정 제외 라벨이 붙어 있는지 */
function hasExcludeLabel(pr, excludeLabel) {
  return (pr.labels ?? []).some((label) => label.name === excludeLabel);
}

/**
 * round-robin 후보 선정: 휴가 아님 + PR 작성자 제외 중
 * last_assigned_at이 가장 오래된 사람(null 최우선), 동률이면 rotation_order 낮은 순.
 */
function pickCandidate(reviewers, prAuthor) {
  const candidates = reviewers.filter(
    (reviewer) =>
      !reviewer.is_on_vacation && reviewer.github_username !== prAuthor,
  );
  if (candidates.length === 0) {
    return null;
  }

  return [...candidates].sort((a, b) => {
    if (a.last_assigned_at === null && b.last_assigned_at !== null) return -1;
    if (a.last_assigned_at !== null && b.last_assigned_at === null) return 1;
    if (a.last_assigned_at !== b.last_assigned_at) {
      return a.last_assigned_at < b.last_assigned_at ? -1 : 1;
    }
    return a.rotation_order - b.rotation_order;
  })[0];
}

function loadNotifyIds() {
  const filePath = path.join(__dirname, '..', 'notify_ids.json');
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

async function run({ github, context, core }) {
  const pr = context.payload.pull_request;
  const setting = await loadSetting(core);

  if (hasExcludeLabel(pr, setting.exclude_label)) {
    core.notice(`PR #${pr.number}: '${setting.exclude_label}' 라벨 — 배정 제외`);
    return;
  }
  if (pr.requested_reviewers && pr.requested_reviewers.length > 0) {
    core.notice(`PR #${pr.number}: 이미 리뷰어가 지정되어 있어 스킵합니다`);
    return;
  }

  const existing = await sbFetch(
    `review_assignment?pr_number=eq.${pr.number}&status=eq.OPEN&select=id`,
  );
  if (existing.length > 0) {
    core.notice(`PR #${pr.number}: 이미 OPEN 배정 기록이 있어 스킵합니다`);
    return;
  }

  const reviewers = await sbFetch('reviewer?select=*');
  const candidate = pickCandidate(reviewers, pr.user.login);
  if (!candidate) {
    core.warning('배정 가능한 리뷰어가 없습니다 (전원 휴가 등)');
    await github.rest.issues.createComment({
      owner: context.repo.owner,
      repo: context.repo.repo,
      issue_number: pr.number,
      body: '🙋 자동 배정 가능한 리뷰어가 없습니다. 리뷰어를 수동으로 지정해주세요.',
    });
    return;
  }

  // GitHub API 지정을 DB 기록보다 먼저 수행 — 실패 시 유령 배정 방지
  await github.rest.pulls.requestReviewers({
    owner: context.repo.owner,
    repo: context.repo.repo,
    pull_number: pr.number,
    reviewers: [candidate.github_username],
  });

  const now = new Date();
  const deadline = new Date(
    now.getTime() + setting.deadline_hours * 60 * 60 * 1000,
  );

  await sbFetch('review_assignment', {
    method: 'POST',
    body: {
      reviewer_id: candidate.id,
      pr_number: pr.number,
      pr_title: pr.title,
      pr_author: pr.user.login,
      pr_url: pr.html_url,
      assigned_at: now.toISOString(),
      deadline_at: deadline.toISOString(),
      status: 'OPEN',
    },
  });
  await sbFetch(`reviewer?id=eq.${candidate.id}`, {
    method: 'PATCH',
    body: {
      last_assigned_at: now.toISOString(),
      updated_at: now.toISOString(),
    },
  });

  const notifyIds = loadNotifyIds();
  const discordId = notifyIds[candidate.github_username];
  core.setOutput('assigned', 'true');
  core.setOutput('reviewer_name', candidate.display_name);
  core.setOutput(
    'reviewer_mention',
    discordId ? `<@${discordId}>` : candidate.github_username,
  );
  core.setOutput('deadline_label', formatDeadline(setting.deadline_hours));
  core.notice(`PR #${pr.number} 리뷰어 배정: ${candidate.github_username}`);
}

module.exports = {
  pickCandidate,
  run,
  formatDeadline,
  hasExcludeLabel,
  DEFAULT_SETTING,
};
