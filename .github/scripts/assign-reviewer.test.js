'use strict';

const { test } = require('node:test');
const assert = require('node:assert/strict');
const {
  pickCandidate,
  formatDeadline,
  hasExcludeLabel,
} = require('./assign-reviewer');

const reviewer = (overrides) => ({
  id: 1,
  github_username: 'user1',
  display_name: '유저1',
  rotation_order: 1,
  is_on_vacation: false,
  last_assigned_at: null,
  ...overrides,
});

test('pickCandidate: 휴가 중인 리뷰어는 제외한다', () => {
  const reviewers = [
    reviewer({ id: 1, github_username: 'a', is_on_vacation: true }),
    reviewer({ id: 2, github_username: 'b' }),
  ];
  assert.equal(pickCandidate(reviewers, 'author').github_username, 'b');
});

test('pickCandidate: PR 작성자는 제외한다', () => {
  const reviewers = [
    reviewer({ id: 1, github_username: 'a' }),
    reviewer({ id: 2, github_username: 'b', last_assigned_at: '2026-01-01T00:00:00Z' }),
  ];
  assert.equal(pickCandidate(reviewers, 'a').github_username, 'b');
});

test('pickCandidate: last_assigned_at이 null인 리뷰어가 최우선이다', () => {
  const reviewers = [
    reviewer({ id: 1, github_username: 'a', last_assigned_at: '2026-01-01T00:00:00Z' }),
    reviewer({ id: 2, github_username: 'b', last_assigned_at: null, rotation_order: 9 }),
  ];
  assert.equal(pickCandidate(reviewers, 'author').github_username, 'b');
});

test('pickCandidate: 가장 오래 전에 배정받은 리뷰어를 선택한다', () => {
  const reviewers = [
    reviewer({ id: 1, github_username: 'a', last_assigned_at: '2026-06-01T00:00:00Z' }),
    reviewer({ id: 2, github_username: 'b', last_assigned_at: '2026-05-01T00:00:00Z' }),
    reviewer({ id: 3, github_username: 'c', last_assigned_at: '2026-07-01T00:00:00Z' }),
  ];
  assert.equal(pickCandidate(reviewers, 'author').github_username, 'b');
});

test('pickCandidate: 동률이면 rotation_order 낮은 순이다', () => {
  const sameTime = '2026-06-01T00:00:00Z';
  const reviewers = [
    reviewer({ id: 1, github_username: 'a', last_assigned_at: sameTime, rotation_order: 3 }),
    reviewer({ id: 2, github_username: 'b', last_assigned_at: sameTime, rotation_order: 1 }),
  ];
  assert.equal(pickCandidate(reviewers, 'author').github_username, 'b');
});

test('pickCandidate: 후보가 없으면 null을 반환한다', () => {
  const reviewers = [
    reviewer({ id: 1, github_username: 'a', is_on_vacation: true }),
    reviewer({ id: 2, github_username: 'author' }),
  ];
  assert.equal(pickCandidate(reviewers, 'author'), null);
});

test('formatDeadline: 24의 배수는 일 단위, 아니면 시간 단위로 표기한다', () => {
  assert.equal(formatDeadline(96), '4일');
  assert.equal(formatDeadline(24), '1일');
  assert.equal(formatDeadline(30), '30시간');
});

test('hasExcludeLabel: 제외 라벨이 있으면 true, 없거나 labels 미존재 시 false', () => {
  assert.equal(
    hasExcludeLabel({ labels: [{ name: 'No Review' }, { name: 'bug' }] }, 'No Review'),
    true,
  );
  assert.equal(hasExcludeLabel({ labels: [{ name: 'bug' }] }, 'No Review'), false);
  assert.equal(hasExcludeLabel({}, 'No Review'), false);
});
