'use strict';

const { sbFetch } = require('./supabase');

/**
 * 배정된 리뷰어가 리뷰를 제출하거나 PR이 닫히면 배정을 완료 처리한다.
 */
async function run({ context, core }) {
  const pr = context.payload.pull_request;
  const targetBases = process.env.TARGET_BASE_BRANCHES.split(',').map((s) =>
    s.trim(),
  );

  if (context.eventName === 'pull_request_review') {
    if (!targetBases.includes(pr.base.ref)) {
      core.notice('배정 대상 브랜치의 PR이 아니므로 스킵합니다');
      return;
    }

    const review = context.payload.review;
    const reviewerLogin = review.user.login;
    if (reviewerLogin === pr.user.login) {
      core.notice('PR 작성자 본인의 리뷰 이벤트는 무시합니다');
      return;
    }

    const reviewers = await sbFetch(
      `reviewer?github_username=eq.${encodeURIComponent(reviewerLogin)}&select=id`,
    );
    if (reviewers.length === 0) {
      core.notice(`${reviewerLogin}은 배정 대상 리뷰어가 아니므로 무시합니다`);
      return;
    }

    const updated = await sbFetch(
      `review_assignment?pr_number=eq.${pr.number}&status=eq.OPEN&reviewer_id=eq.${reviewers[0].id}`,
      {
        method: 'PATCH',
        body: { status: 'CLOSED', completed_at: review.submitted_at },
      },
    );
    if (updated && updated.length > 0) {
      core.notice(`PR #${pr.number} 배정 완료 처리 (리뷰 제출: ${reviewerLogin})`);
    }
    return;
  }

  // pull_request closed (merge 포함): OPEN 배정 일괄 정리 — orphan 방지
  const closedAt = pr.closed_at ?? new Date().toISOString();
  const updated = await sbFetch(
    `review_assignment?pr_number=eq.${pr.number}&status=eq.OPEN`,
    {
      method: 'PATCH',
      body: { status: 'CLOSED', completed_at: closedAt },
    },
  );
  if (updated && updated.length > 0) {
    core.notice(`PR #${pr.number} close로 OPEN 배정 ${updated.length}건 정리`);
  }
}

module.exports = { run };
