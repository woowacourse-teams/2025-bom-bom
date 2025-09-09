import { GetArticleByIdResponse } from '@/apis/articles';

export type GuideMail = GetArticleByIdResponse & {
  articleId: number;
  contentSummary: string;
};

export const GUIDE_MAILS: GuideMail[] = [
  {
    articleId: 1,
    title: '[봄봄 가이드] 뉴스레터 구독하기',
    contents:
      '<div style="max-width:680px;margin:0 auto;padding:24px 20px;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Apple SD Gothic Neo,Malgun Gothic,Arial,sans-serif;line-height:1.65;color:#1f2937;"><div style="background:#fff7ed;border:1px solid #ffedd5;border-radius:16px;padding:20px 18px;margin-bottom:20px;"><div style="font-size:14px;color:#fb923c;font-weight:700;margin-bottom:6px;">봄봄 사용 가이드</div><h1 style="margin:0 0 10px;font-size:22px;color:#111827;">뉴스레터 구독하기</h1><p style="margin:0;color:#374151;">관심 뉴스레터를 선택하고 구독해서 <strong>봄봄</strong>에서 받아보세요.</p></div><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">🔍 뉴스레터 둘러보기</h2><div style="border-left:4px solid #f97316;padding-left:16px;margin-bottom:16px;"><p style="margin:0 0 8px;color:#374151;">상단 네비게이션에서 <strong>「뉴스레터 추천」</strong> 페이지로 이동해보세요.<br/>관심 카테고리를 선택하고 발행 사이트 리스트를 확인할 수 있어요.<br/>원하는 사이트를 클릭해서 상세 소개를 확인해보세요.</p></div><div style="background:#fef3c7;border:1px solid #fbbf24;border-radius:8px;padding:12px;margin-bottom:16px;"><strong style="color:#92400e;">⚠️ 주의사항</strong><br/><span style="color:#451a03;">각 뉴스레터의 발행 요일을 꼭 확인해주세요.</span></div><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">📧 구독 페이지 접속하기</h2><p style="margin:0 0 12px;color:#374151;">원하는 뉴스레터에서 <strong>「구독하기」</strong> 버튼을 눌러 구독 페이지로 이동해보세요.</p><img src="/assets/guide_subscribe_1.gif" style="width: 640px;" alt="뉴스레터 구독 - 1" /><div style="background:#fef3c7;border:1px solid #fbbf24;border-radius:8px;padding:12px;margin-bottom:16px;"><strong style="color:#92400e;">⚠️ 주의사항</strong><br/><span style="color:#451a03;">버튼을 누르면 봄봄 메일이 자동으로 복사돼요.</span></div><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">📝 봄봄 메일 붙여넣기</h2><p style="margin:0 0 12px;color:#374151;">구독 페이지의 이메일 입력 칸에 복사된 봄봄 메일을 붙여넣기 해주세요.<br/>봄봄 계정으로 접속 중이라면 자동으로 붙여넣기가 가능해요.</p><img src="/assets/guide_subscribe_2.gif" style="width: 640px;" alt="뉴스레터 구독 - 3" /><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">✅ 구독 완료</h2><p style="margin:0 0 12px;color:#374151;">🎉 축하해요! 이제 정기적으로 뉴스레터를 받아보실 수 있어요.</p><img src="/assets/guide_subscribe_3.gif" style="width: 640px;" alt="뉴스레터 구독 - 2" /><div style="background:#fef3c7;border:1px solid #fbbf24;border-radius:8px;padding:12px;margin-bottom:16px;"><strong style="color:#92400e;">⚠️ 주의사항</strong><br/><span style="color:#451a03;">일부 뉴스레터는 환영 메일을 따로 발송하지 않을 수 있어요.<br/>구독 후에는 <strong>「오늘의 뉴스레터」</strong>에서 도착한 아티클을 깔끔하게 확인할 수 있어요.</span></div><div style="text-align:center;margin:24px 0 8px;"><a href="#" style="display:inline-block;background:#f97316;color:#fff;text-decoration:none;padding:11px 16px;border-radius:999px;font-weight:700;">지금 구독하러 가기</a></div></div>',
    contentSummary:
      '뉴스레터 추천에서 관심 카테고리 선택, 구독 페이지 접속, 봄봄 메일 붙여넣기까지 단계별 구독 방법을 안내합니다.',
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
    articleId: 2,
    title: '[봄봄 가이드] 키우기',
    contents:
      '<div style="max-width:680px;margin:0 auto;padding:24px 20px;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Apple SD Gothic Neo,Malgun Gothic,Arial,sans-serif;line-height:1.65;color:#1f2937;"><div style="background:#fff7ed;border:1px solid #ffedd5;border-radius:16px;padding:20px 18px;margin-bottom:20px;"><div style="font-size:14px;color:#fb923c;font-weight:700;margin-bottom:6px;">봄봄 사용 가이드</div><h1 style="margin:0 0 10px;font-size:22px;color:#111827;">키우기</h1><p style="margin:0;color:#374151;">봄이랑 함께하는 읽기 모험! 매일 출석하고 아티클을 읽으면 귀여운 봄이가 쑥쑥 자라나요.</p></div><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">🌱 키우기 가이드</h2><p style="margin:0 0 16px;color:#374151;">봄이랑 함께하는 읽기 모험!<br/>매일 출석하고 아티클을 읽으면 귀여운 봄이가 쑥쑥 자라나요.<br/><strong>레벨 5까지 성장</strong>시켜 보세요! 🌱</p><img src="/assets/guide_grow.gif" alt="가이드 영상" /><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">🍀 경험치 모으는 방법</h2><ul style="margin:0 0 16px 18px;padding:0;color:#374151;"><li style="margin:8px 0;"><strong>🗓 출석 체크</strong>: 하루 한 번, 봄이에게 인사만 해도 경험치가 쌓여요!</li><li style="margin:8px 0;"><strong>📖 아티클 읽기</strong>: 오늘 도착한 아티클을 읽으면 (최대 3개) 추가 경험치를 얻을 수 있어요!</li><li style="margin:8px 0;"><strong>🎁 연속 보너스</strong>: 7일 이상 꾸준히 읽으면 보너스 점수까지 받을 수 있어요!<br/><span style="color:#6b7280;font-size:14px;">(아티클이 안 온 날은 연속이 끊기지 않아요, 안심하세요 💤)</span></li></ul><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">📊 내 진행 상황 보기</h2><p style="margin:0 0 16px;color:#374151;">오른쪽 패널에서 다음 정보를 바로 확인할 수 있어요:</p><ul style="margin:0 0 16px 18px;padding:0;color:#374151;"><li style="margin:6px 0;">✨ 연속 읽은 일수</li><li style="margin:6px 0;">✨ 오늘의 읽기 진행률</li><li style="margin:6px 0;">✨ 주간 목표 달성률</li></ul><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">💡 이렇게 즐겨보세요</h2><ol style="margin:0 0 16px 18px;padding:0;color:#374151;"><li style="margin:8px 0;">1. 오늘의 뉴스레터 ✉️ 확인하기</li><li style="margin:8px 0;">2. 아티클 읽고 경험치 챙기기 📚</li><li style="margin:8px 0;">3. 봄이가 레벨업하는 모습 구경하기 🐰💕</li><li style="margin:8px 0;">4. 주간 목표 달성률 보며 뿌듯해하기 🌟</li></ol><div style="background:#f0f9ff;border:1px solid #bae6fd;border-radius:12px;padding:16px;margin:20px 0;"><p style="margin:0;color:#0284c7;font-size:14px;line-height:1.6;">작은 읽기가 모여 큰 성장이 돼요.<br/>오늘도 봄이랑 같이 시작해볼까요? 🌱</p></div><div style="text-align:center;margin:24px 0 8px;"><a href="#" style="display:inline-block;background:#f97316;color:#fff;text-decoration:none;padding:11px 16px;border-radius:999px;font-weight:700;">출석하고 읽기 시작하기</a></div></div>',
    contentSummary:
      '봄이 캐릭터와 함께하는 읽기 습관 만들기. 출석과 아티클 읽기로 경험치를 모아 레벨 5까지 성장시키는 방법을 안내합니다.',
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
    articleId: 3,
    title: '[봄봄 가이드] 하이라이트 & 메모 사용법',
    contents:
      '<div style="max-width:680px;margin:0 auto;padding:24px 20px;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Apple SD Gothic Neo,Malgun Gothic,Arial,sans-serif;line-height:1.65;color:#1f2937;"><div style="background:#fff7ed;border:1px solid #ffedd5;border-radius:16px;padding:20px 18px;margin-bottom:20px;"><div style="font-size:14px;color:#fb923c;font-weight:700;margin-bottom:6px;">봄봄 사용 가이드</div><h1 style="margin:0 0 10px;font-size:22px;color:#111827;">하이라이트 & 메모 사용법</h1><p style="margin:0;color:#374151;">읽기 중 중요한 구절을 <strong>하이라이트</strong>하고 <strong>개인 메모</strong>를 남겨서 지식을 체계적으로 정리해보세요.</p></div><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">🎯 중요한 문장만 쏙! 하이라이트 & 메모</h2><p style="margin:0 0 16px;color:#374151;">읽기 중 중요한 구절을 <strong>하이라이트</strong>하고 <strong>개인 메모</strong>를 남겨서 지식을 체계적으로 정리해보세요.</p><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">🖍️ 하이라이트 만들기</h2><div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:20px;margin-bottom:20px;"><p style="margin:0 0 16px;color:#475569;font-size:15px;">아티클에서 중요한 구절을 마우스로 드래그해서 선택하면 퀵 메뉴가 나타나요.<br/><strong>하이라이트</strong> 버튼을 클릭하면 선택한 텍스트가 노란색으로 강조 표시돼요.</p><div style="text-align:center;margin-bottom:12px;"><video autoPlay loop muted playsInline style="max-width:100%;height:auto;border-radius:8px;border:1px solid #e2e8f0;"><source src="/assets/guide_highlight.mp4" type="video/mp4" />텍스트 선택 후 하이라이트 버튼 클릭하는 모습</video></div><div style="background:#ecfdf5;border:1px solid #bbf7d0;border-radius:8px;padding:12px;"><div style="display:flex;align-items:center;"><span style="color:#059669;font-size:16px;">✅</span><strong style="margin-left:8px;color:#065f46;">완료!</strong><span style="margin-left:8px;color:#065f46;">이제 중요한 구절이 하이라이트되었어요.</span></div></div></div><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">📝 메모 추가하기</h2><div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:20px;margin-bottom:24px;"><p style="margin:0 0 16px;color:#475569;font-size:15px;">텍스트를 선택한 후 퀵 메뉴에서 <strong>메모</strong> 버튼을 클릭하면 메모 입력창이 나타나요.<br/>해당 구절에 대한 생각, 질문, 아이디어를 자유롭게 작성해보세요.</p><div style="text-align:center;"><video autoPlay loop muted playsInline style="max-width:100%;height:auto;border-radius:8px;border:1px solid #e2e8f0;"><source src="/assets/guide_memo.mp4" type="video/mp4" />메모 버튼 클릭 후 메모 작성하는 모습</video></div></div><h2 style="font-size:18px;margin:24px 0 12px;color:#111827;">📚 내 하이라이트 & 메모 모아보기</h2><div style="border-left:4px solid #f97316;padding-left:16px;margin-bottom:16px;"><p style="margin:0 0 8px;color:#374151;font-weight:600;"><strong>경로</strong>: 좌측 바로가기에서 한 번에 확인할 수 있어요.</p></div><ul style="margin:0 0 20px 18px;padding:0;color:#374151;"><li style="margin:8px 0;"><strong>🖍️ 하이라이트</strong>: 중요 구절만 모아본 목록을 확인할 수 있어요</li><li style="margin:8px 0;"><strong>📝 메모</strong>: 내가 남긴 메모가 포함된 하이라이트 목록을 볼 수 있어요</li></ul><div style="border:2px dashed #cbd5e1;background:#f1f5f9;padding:12px;border-radius:8px;text-align:center;color:#64748b;font-style:italic;margin-bottom:20px;"><video autoPlay loop muted playsInline style="max-width:100%;height:auto;border-radius:8px;border:1px solid #e2e8f0;"><source src="/assets/guide_quick_menu.mp4" type="video/mp4" />메모 버튼 클릭 후 메모 작성하는 모습</video></div><div style="background:#f0f9ff;border:1px solid #bae6fd;border-radius:12px;padding:16px;margin:20px 0;"><div style="color:#0369a1;font-weight:600;margin-bottom:8px;">🎓 학습 효과 UP!</div><p style="margin:0;color:#0284c7;font-size:14px;line-height:1.6;">하이라이트와 메모를 활용하면 단순 읽기를 넘어 <strong>능동적 학습</strong>이 가능해요.<br/>나만의 지식 아카이브를 만들어서 언제든 다시 찾아볼 수 있어요!</p></div><div style="text-align:center;margin:24px 0 8px;"><a href="#" style="display:inline-block;background:#f97316;color:#fff;text-decoration:none;padding:12px 20px;border-radius:999px;font-weight:700;font-size:15px;">지금 바로 하이라이트 해보기</a></div></div>',
    contentSummary:
      '텍스트 드래그로 하이라이트 만들기, 메모 추가하기, 좌측 바로가기에서 하이라이트 & 메모 모아보기까지 능동적 학습 방법을 안내합니다.',
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
