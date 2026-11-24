package org.spock.companion;
import java.util.Locale;
/** Pure deterministic routing. Recognized commands only prepare user-confirmed handoffs. */
final class CommandRules {
    static String normalized(String s){return s==null?"":s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]"," ").replaceAll("\\s+"," ").trim().replaceFirst("^mr ","mister ");}
    static String cleanNumber(String s){return s==null?"":s.trim().replaceAll("[\\s().-]", "");}
    static String phMobile(String s){String n=cleanNumber(s);if(n.matches("09[0-9]{9}"))return "+63"+n.substring(1);if(n.matches("\\+639[0-9]{9}"))return n;return null;}
    static String route(String spoken,String wake){String n=normalized(spoken),w=normalized(wake);if(!w.isEmpty()&&n.equals(w))return "CHATGPT";switch(n){case "open chatgpt":case "open spock":return "CHATGPT";case "open workspace":return "WORKSPACE";case "open google chat":return "GOOGLE_CHAT";case "open dialer":return "DIAL";case "prepare sms":return "SMS";case "prepare whatsapp":return "WHATSAPP";case "security analysis":return "SECURITY";default:return "NONE";}}
}
