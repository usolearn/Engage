package org.spock.companion;
public class CommandRulesTest {
    private static int count=0;
    private static void eq(Object expected,Object actual){count++;if(!java.util.Objects.equals(expected,actual))throw new AssertionError("Expected "+expected+", got "+actual);}
    public static void main(String[] args){
        String wake="Mr. Spock, analysis";
        eq("CHATGPT",CommandRules.route("Mister Spock analysis",wake));
        eq("CHATGPT",CommandRules.route("MR. SPOCK, ANALYSIS!",wake));
        eq("CHATGPT",CommandRules.route("open spock",wake));
        eq("WORKSPACE",CommandRules.route("open workspace",wake));
        eq("GOOGLE_CHAT",CommandRules.route("open Google Chat",wake));
        eq("DIAL",CommandRules.route("open dialer",wake));
        eq("SMS",CommandRules.route("prepare SMS",wake));
        eq("WHATSAPP",CommandRules.route("prepare WhatsApp",wake));
        eq("SECURITY",CommandRules.route("security analysis",wake));
        eq("NONE",CommandRules.route("do not prepare SMS",wake));
        eq("NONE",CommandRules.route("send SMS now",wake));
        eq("NONE",CommandRules.route("", ""));
        eq("NONE",CommandRules.route(null,wake));
        eq("+639171234567",CommandRules.phMobile("0917-123-4567"));
        eq("+639171234567",CommandRules.phMobile("+63 917 123 4567"));
        eq(null,CommandRules.phMobile("+15091234567"));
        eq(null,CommandRules.phMobile("09171234567;09171234568"));
        eq(null,CommandRules.phMobile("0917"));
        eq("+15091234567",CommandRules.cleanNumber("+1 (509) 123-4567"));
        eq("CHATGPT",CommandRules.route("engage", "engage"));
        System.out.println("PASS: "+count+" command and phone rules");
    }
}
