public class ExceptionsSearch {
    enum ExceptForAnotherBase {
        GPLUS("plusone.google.com"),
        FACEBOOK("www.facebook.com"), TWITTER("twitter.com");


        public String exceptString;

        ExceptForAnotherBase(String partOfLink) {
            this.exceptString = partOfLink;
        }

        public static boolean checkExceptions(String link) {
            for (ExceptForAnotherBase except : ExceptForAnotherBase.values()) {
                if (link.contains(except.exceptString) || link == null)
                    return false;
            }
            return true;
        }
    }

    enum ExceptForLinksOnPage {
        JAVASCRIPT("javascript"), ENCODING_IMG("data:image"), HASHES("#");

        public String exceptString;

        ExceptForLinksOnPage(String partOfLink) {
            this.exceptString = partOfLink;
        }

        public static boolean checkExceptions(String link) {
            boolean result = true;
            for (ExceptForLinksOnPage except : ExceptForLinksOnPage.values()) {
                if (link.contains(except.exceptString) || link.startsWith(HASHES.exceptString))
                    result = false;
            }
            return result;
        }
    }

    enum ExceptForExternalSite {
        ADOBE("adobe"), MAILTO("mailto"), BR_URL("//br."), STAT("//stat."), FACEBOOK("www.facebook.com"), TWITTER(
                "twitter.com"), QA_KR("https://kr.4game.com.qa2"),
        DEMO_BR("//demo.ru-br"), DEMO_STAT("//demo.ru-stat"), DEMO_KR("//demo.kr"), DEMO_SUPPORT(
                "//demo.support"), STORE_DEV("ru-store");


        public String exceptString;

        ExceptForExternalSite(String partOfLink) {
            this.exceptString = partOfLink;
        }

        public static boolean checkExceptions(String link) {
            for (ExceptForExternalSite except : ExceptForExternalSite.values()) {
                if (link.contains(except.exceptString) || link == null)
                    return false;
            }
            return true;
        }
    }


    enum SpecialPageLinks {
        CSS("css"), SLASH("/");
        String link;

        SpecialPageLinks(String s) {
            link = s;
        }

        public static boolean checkLinks(String link) {
            for (SpecialPageLinks a : SpecialPageLinks.values())
                if (link.contains(a.link))
                    return true;

            return false;

        }
    }

}
