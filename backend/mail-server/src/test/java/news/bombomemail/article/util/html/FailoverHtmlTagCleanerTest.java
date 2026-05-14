package news.bombomemail.article.util.html;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FailoverHtmlTagCleanerTest {

    @Test
    @DisplayName("첫 번째 cleaner가 성공하면 그 결과를 반환한다")
    void 첫_번째_cleaner가_성공하면_그_결과를_반환한다() {
        // given
        String html = "<p>테스트</p>";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner(),
                new RegexHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).isEqualTo("테스트");
    }

    @Test
    @DisplayName("첫 번째 cleaner가 실패하면 두 번째 cleaner를 시도한다")
    void 첫_번째_cleaner가_실패하면_두_번째_cleaner를_시도한다() {
        // given
        String html = "<p>테스트</p>";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new ThrowingHtmlTagCleaner(),
                new RegexHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).isEqualTo("테스트");
    }

    @Test
    @DisplayName("첫 번째 cleaner가 null을 반환하면 두 번째 cleaner를 시도한다")
    void 첫_번째_cleaner가_null을_반환하면_두_번째_cleaner를_시도한다() {
        // given
        String html = "<p>테스트</p>";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new NullReturningHtmlTagCleaner(),
                new RegexHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).isEqualTo("테스트");
    }

    @Test
    @DisplayName("모든 cleaner가 실패하면 원본 HTML을 반환한다")
    void 모든_cleaner가_실패하면_원본_HTML을_반환한다() {
        // given
        String html = "<p>테스트</p>";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new ThrowingHtmlTagCleaner(),
                new ThrowingHtmlTagCleaner(),
                new ThrowingHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).isEqualTo(html);
    }

    @Test
    @DisplayName("모든 cleaner가 null을 반환하면 원본 HTML을 반환한다")
    void 모든_cleaner가_null을_반환하면_원본_HTML을_반환한다() {
        // given
        String html = "<p>테스트</p>";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new NullReturningHtmlTagCleaner(),
                new NullReturningHtmlTagCleaner(),
                new NullReturningHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).isEqualTo(html);
    }

    @Test
    @DisplayName("빈 문자열을 입력하면 빈 문자열을 반환한다")
    void 빈_문자열을_입력하면_빈_문자열을_반환한다() {
        // given
        String html = "";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null을 입력하면 빈 문자열을 반환한다")
    void null을_입력하면_빈_문자열을_반환한다() {
        // given
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("공백만 있는 문자열을 입력하면 빈 문자열을 반환한다")
    void 공백만_있는_문자열을_입력하면_빈_문자열을_반환한다() {
        // given
        String html = "   ";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("cleaner가 null이면 예외를 발생시킨다")
    void cleaner가_null이면_예외를_발생시킨다() {
        // when & then
        assertThatThrownBy(() -> new FailoverHtmlTagCleaner((HtmlTagCleaner[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("적어도 하나 이상의 HtmlTagCleaner가 필요합니다");
    }

    @Test
    @DisplayName("cleaner 배열이 비어있으면 예외를 발생시킨다")
    void cleaner_배열이_비어있으면_예외를_발생시킨다() {
        // when & then
        assertThatThrownBy(() -> new FailoverHtmlTagCleaner())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("적어도 하나 이상의 HtmlTagCleaner가 필요합니다");
    }

    @Test
    @DisplayName("여러 cleaner 중 중간에 있는 cleaner가 성공하면 그 결과를 반환한다")
    void 여러_cleaner_중_중간에_있는_cleaner가_성공하면_그_결과를_반환한다() {
        // given
        String html = "<p>테스트</p>";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new ThrowingHtmlTagCleaner(),
                new RegexHtmlTagCleaner(),
                new JsoupHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).isEqualTo("테스트");
    }

    @Test
    @DisplayName("실제 구현체들을 사용하여 HTML 태그를 제거한다")
    void 실제_구현체들을_사용하여_HTML_태그를_제거한다() {
        // given
        String html = "<div><p>안녕하세요</p><span>테스트</span></div>";
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner(),
                new JFiveTextExtractor(),
                new RegexHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).contains("안녕하세요");
        assertThat(result).contains("테스트");
        assertThat(result).doesNotContain("<");
        assertThat(result).doesNotContain(">");
    }

    @Test
    @DisplayName("script 태그가 포함된 복잡한 HTML을 처리한다")
    void script_태그가_포함된_복잡한_HTML을_처리한다() {
        // given
        String html = """
            <html>
                <head>
                    <script>
                        function test() {
                            console.log('test');
                        }
                    </script>
                    <style>
                        body { color: red; }
                    </style>
                </head>
                <body>
                    <h1>제목입니다</h1>
                    <p>본문 내용입니다.</p>
                    <div>
                        <span>중첩된 텍스트</span>
                    </div>
                </body>
            </html>
            """;
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner(),
                new JFiveTextExtractor(),
                new RegexHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).contains("제목입니다");
        assertThat(result).contains("본문 내용입니다");
        assertThat(result).contains("중첩된 텍스트");
        assertThat(result).doesNotContain("console.log");
        assertThat(result).doesNotContain("function test");
        assertThat(result).doesNotContain("color: red");
        assertThat(result).doesNotContain("<script>");
        assertThat(result).doesNotContain("</script>");
        assertThat(result).doesNotContain("<style>");
        assertThat(result).doesNotContain("</style>");
    }

    @Test
    @DisplayName("인라인 스타일과 속성이 있는 복잡한 HTML을 처리한다")
    void 인라인_스타일과_속성이_있는_복잡한_HTML을_처리한다() {
        // given
        String html = """
            <div class="container" id="main" style="color: blue;">
                <h2 class="title">뉴스레터 제목</h2>
                <p class="content" data-id="123">
                    이메일 본문 내용입니다.
                    <a href="https://example.com" target="_blank">링크 텍스트</a>
                </p>
                <ul>
                    <li>리스트 항목 1</li>
                    <li>리스트 항목 2</li>
                    <li>리스트 항목 3</li>
                </ul>
                <table>
                    <tr>
                        <th>헤더 1</th>
                        <th>헤더 2</th>
                    </tr>
                    <tr>
                        <td>데이터 1</td>
                        <td>데이터 2</td>
                    </tr>
                </table>
            </div>
            """;
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner(),
                new JFiveTextExtractor(),
                new RegexHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).contains("뉴스레터 제목");
        assertThat(result).contains("이메일 본문 내용입니다");
        assertThat(result).contains("링크 텍스트");
        assertThat(result).contains("리스트 항목 1");
        assertThat(result).contains("리스트 항목 2");
        assertThat(result).contains("리스트 항목 3");
        assertThat(result).contains("헤더 1");
        assertThat(result).contains("헤더 2");
        assertThat(result).contains("데이터 1");
        assertThat(result).contains("데이터 2");
        assertThat(result).doesNotContain("class=");
        assertThat(result).doesNotContain("style=");
        assertThat(result).doesNotContain("href=");
        assertThat(result).doesNotContain("<");
        assertThat(result).doesNotContain(">");
    }

    @Test
    @DisplayName("여러 script와 style 태그가 섞인 복잡한 HTML을 처리한다")
    void 여러_script와_style_태그가_섞인_복잡한_HTML을_처리한다() {
        // given
        String html = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <title>뉴스레터</title>
                <script type="text/javascript">
                    var trackingId = 'abc123';
                    window.track = function() { return true; };
                </script>
                <style type="text/css">
                    .header { background: #fff; }
                    .footer { margin-top: 20px; }
                </style>
            </head>
            <body>
                <header class="header">
                    <h1>뉴스레터 헤더</h1>
                </header>
                <main>
                    <article>
                        <h2>오늘의 뉴스</h2>
                        <p>중요한 뉴스 내용이 여기에 있습니다.</p>
                        <script>
                            // 인라인 스크립트
                            alert('test');
                        </script>
                    </article>
                </main>
                <footer class="footer">
                    <p>© 2025 뉴스레터</p>
                </footer>
            </body>
            </html>
            """;
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner(),
                new JFiveTextExtractor(),
                new RegexHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).contains("뉴스레터 헤더");
        assertThat(result).contains("오늘의 뉴스");
        assertThat(result).contains("중요한 뉴스 내용이 여기에 있습니다");
        assertThat(result).contains("© 2025 뉴스레터");
        assertThat(result).doesNotContain("trackingId");
        assertThat(result).doesNotContain("window.track");
        assertThat(result).doesNotContain("alert");
        assertThat(result).doesNotContain("background");
        assertThat(result).doesNotContain("margin-top");
        assertThat(result).doesNotContain("<script");
        assertThat(result).doesNotContain("</script>");
        assertThat(result).doesNotContain("<style");
        assertThat(result).doesNotContain("</style>");
    }

    @Test
    @DisplayName("특수문자와 이모지가 포함된 HTML을 처리한다")
    void 특수문자와_이모지가_포함된_HTML을_처리한다() {
        // given
        String html = """
            <div>
                <p>특수문자: &lt; &gt; &amp; &quot; &apos;</p>
                <p>이모지: 😀 🎉 🚀</p>
                <p>한글/영문 혼합: Hello 안녕하세요 123</p>
                <p>줄바꿈이 있는<br/>텍스트</p>
            </div>
            """;
        FailoverHtmlTagCleaner failoverCleaner = new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner(),
                new JFiveTextExtractor(),
                new RegexHtmlTagCleaner()
        );

        // when
        String result = failoverCleaner.clean(html);

        // then
        assertThat(result).contains("특수문자");
        assertThat(result).contains("이모지");
        assertThat(result).contains("😀");
        assertThat(result).contains("🎉");
        assertThat(result).contains("🚀");
        assertThat(result).contains("Hello 안녕하세요 123");
        assertThat(result).contains("줄바꿈이 있는");
        assertThat(result).contains("텍스트");
        assertThat(result).doesNotContain("<br");
        assertThat(result).doesNotContain("&lt;");
        assertThat(result).doesNotContain("&gt;");
    }

    // 테스트용 Helper 클래스들
    private static class ThrowingHtmlTagCleaner implements HtmlTagCleaner {
        @Override
        public String clean(String html) {
            throw new RuntimeException("테스트용 예외");
        }
    }

    private static class NullReturningHtmlTagCleaner implements HtmlTagCleaner {
        @Override
        public String clean(String html) {
            return null;
        }
    }
}

