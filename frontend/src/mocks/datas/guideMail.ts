import { GetArticleByIdResponse } from '@/apis/articles';

export type GuideMail = GetArticleByIdResponse & {
  guideId: number;
  contentSummary: string;
};

export const GUIDE_MAILS: GuideMail[] = [
  {
    guideId: 1,
    title: '[봄봄 가이드] 구독 시작하기',
    contents:
      '<div style="max-width:680px;margin:0 auto;padding:24px 20px;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Apple SD Gothic Neo,Malgun Gothic,Arial,sans-serif;line-height:1.65;color:#1f2937;"><div style="background:#fff7ed;border:1px solid #ffedd5;border-radius:16px;padding:20px 18px;margin-bottom:20px;"><div style="font-size:14px;color:#fb923c;font-weight:700;margin-bottom:6px;">봄봄 시작 가이드</div><h1 style="margin:0 0 10px;font-size:22px;color:#111827;">뉴스레터 구독, 추천에서 바로 시작하기</h1><p style="margin:0;color:#374151;">메일함이 아닌 <strong>봄봄</strong>에서 관심 뉴스레터를 구독하고 받아보세요.</p></div><h2 style="font-size:18px;margin:24px 0 8px;color:#111827;">구독 방법</h2><div style="border-left:4px solid #f97316;padding-left:12px;margin-bottom:10px;color:#374151;"><strong>경로:</strong> 상단 네비게이션 ▶ <strong>뉴스레터 추천</strong></div><ol style="margin:0 0 18px 18px;padding:0;color:#374151;"><li style="margin:8px 0;">관심 카테고리를 선택하고 발행 사이트 리스트를 살펴봅니다.</li><li style="margin:8px 0;">원하는 사이트를 클릭해 <strong>상세 소개</strong>를 확인합니다.</li><li style="margin:8px 0;"><strong>구독</strong> 버튼을 눌러 봄봄에서 해당 뉴스레터를 받아보세요.</li></ol><div style="background:#f9fafb;border:1px solid #e5e7eb;border-radius:10px;padding:12px 14px;color:#374151;margin:14px 0 24px;"><strong>Tip</strong> · 구독 후에는 <strong>오늘의 뉴스레터</strong>에서 당일 도착한 아티클만 깔끔하게 확인할 수 있어요.</div><div style="text-align:center;margin:24px 0 8px;"><a href="#" style="display:inline-block;background:#f97316;color:#fff;text-decoration:none;padding:11px 16px;border-radius:999px;font-weight:700;">지금 구독하러 가기</a></div></div>',
    contentSummary:
      '관심 뉴스레터를 선택하고 구독하는 방법을 소개합니다. 추천 페이지에서 손쉽게 시작해 보세요.',
    arrivedDateTime: '2025-08-18T11:00:00+09:00',
    expectedReadTime: 2,
    isRead: false,
    newsletter: {
      email: 'bombom-guide@bombom.news',
      category: '가이드',
      name: '봄봄',
    },
  },
  {
    guideId: 2,
    title: '[봄봄 가이드] 봄 키우기 & 레벨업',
    contents:
      '<div style="max-width:680px;margin:0 auto;padding:24px 20px;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Apple SD Gothic Neo,Malgun Gothic,Arial,sans-serif;line-height:1.65;color:#1f2937;"><div style="background:#fff7ed;border:1px solid #ffedd5;border-radius:16px;padding:20px 18px;margin-bottom:20px;"><div style="font-size:14px;color:#fb923c;font-weight:700;margin-bottom:6px;">봄봄 시작 가이드</div><h1 style="margin:0 0 10px;font-size:22px;color:#111827;">봄과 함께 읽기 습관 만들기</h1><p style="margin:0;color:#374151;">출석과 읽기 완료로 <strong>경험치</strong>를 얻고, 캐릭터 <strong>봄</strong>을 <strong>레벨 5</strong>까지 키워 보세요.</p></div><h2 style="font-size:18px;margin:24px 0 8px;color:#111827;">경험치 얻는 방법</h2><ul style="margin:0 0 12px 18px;padding:0;color:#374151;"><li style="margin:6px 0;"><strong>출석</strong> 체크 시 경험치 획득</li><li style="margin:6px 0;"><strong>아티클 읽기 완료</strong> 시 경험치 획득</li></ul><p style="margin:0 0 14px;color:#6b7280;">우측 패널에서 <strong>연속 읽은 날짜</strong>, <strong>오늘의 진행률</strong>, <strong>주간 목표 진행률</strong>을 확인할 수 있어요.</p><h2 style="font-size:18px;margin:18px 0 8px;color:#111827;">추천 사용 흐름</h2><ol style="margin:0 0 18px 18px;padding:0;color:#374151;"><li style="margin:8px 0;">매일 <strong>오늘의 뉴스레터</strong>에서 도착한 아티클을 확인</li><li style="margin:8px 0;">읽기 완료로 경험치 적립 → 봄 레벨업</li><li style="margin:8px 0;">주간 목표 진행률을 보며 루틴 유지</li></ol><div style="text-align:center;margin:24px 0 8px;"><a href="#" style="display:inline-block;background:#f97316;color:#fff;text-decoration:none;padding:11px 16px;border-radius:999px;font-weight:700;">출석하고 읽기 시작하기</a></div></div>',
    contentSummary:
      '출석과 아티클 읽기로 경험치를 얻고, 캐릭터 봄을 레벨 5까지 성장시키는 방법을 안내합니다.',
    arrivedDateTime: '2025-08-18T11:05:00+09:00',
    expectedReadTime: 2,
    isRead: false,
    newsletter: {
      email: 'bombom-guide@bombom.news',
      category: '가이드',
      name: '봄봄',
    },
  },
  {
    guideId: 3,
    title: '[봄봄 가이드] 하이라이트 & 메모 사용법',
    contents:
      '<div style="max-width:680px;margin:0 auto;padding:24px 20px;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Apple SD Gothic Neo,Malgun Gothic,Arial,sans-serif;line-height:1.65;color:#1f2937;"><div style="background:#fff7ed;border:1px solid #ffedd5;border-radius:16px;padding:20px 18px;margin-bottom:20px;"><div style="font-size:14px;color:#fb923c;font-weight:700;margin-bottom:6px;">봄봄 시작 가이드</div><h1 style="margin:0 0 10px;font-size:22px;color:#111827;">중요한 문장만 쏙! 하이라이트 & 메모</h1><p style="margin:0;color:#374151;">읽기 페이지에서 Selection API 기반 <strong>하이라이트</strong>와 <strong>메모</strong>로 지식을 내 것으로 만드세요.</p></div><h2 style="font-size:18px;margin:24px 0 8px;color:#111827;">사용 방법</h2><ol style="margin:0 0 14px 18px;padding:0;color:#374151;"><li style="margin:8px 0;"><strong>텍스트를 드래그</strong>하면 퀵 메뉴가 나타납니다.</li><li style="margin:8px 0;"><strong>하이라이트</strong> 를 눌러 강조 표시</li><li style="margin:8px 0;"><strong>메모</strong> 를 눌러 해당 하이라이트에 코멘트 작성</li></ol><div style="background:#f9fafb;border:1px solid #e5e7eb;border-radius:10px;padding:12px 14px;color:#374151;margin-bottom:18px;"><strong>Tip</strong> · 아티클 전체는 <strong>북마크</strong>로 저장하고, 중요한 구절은 <strong>하이라이트/메모</strong>로 남겨두세요.</div><h2 style="font-size:18px;margin:18px 0 8px;color:#111827;">모아보기</h2><ul style="margin:0 0 10px 18px;padding:0;color:#374151;"><li style="margin:6px 0;">상단 네비 ▶ <strong>뉴스레터 보관함</strong> 이동</li><li style="margin:6px 0;">왼쪽 <strong>바로 가기</strong>에서 <strong>북마크</strong> · <strong>메मो</strong> 항목으로 한 번에 확인</li></ul><div style="text-align:center;margin:24px 0 8px;"><a href="#" style="display:inline-block;background:#f97316;color:#fff;text-decoration:none;padding:11px 16px;border-radius:999px;font-weight:700;">하이라이트/메모 해보기</a></div></div>',
    contentSummary:
      '읽기 페이지에서 텍스트를 하이라이트하고 메모를 남기는 기능을 활용하는 방법을 알려드립니다.',
    arrivedDateTime: '2025-08-18T11:10:00+09:00',
    expectedReadTime: 3,
    isRead: false,
    newsletter: {
      email: 'bombom-guide@bombom.news',
      category: '가이드',
      name: '봄봄',
    },
  },
];
