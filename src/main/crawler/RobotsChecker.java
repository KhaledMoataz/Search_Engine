package main.crawler;

import main.utilities.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RobotsChecker {

    private ConcurrentHashMap<String, RobotsRules> concMap;

    public RobotsChecker() {
        this.concMap = new ConcurrentHashMap<String, RobotsRules>();
    }

    private class RobotsRules {
        public List<String> disallowedUrls;
        public boolean ready;

        public RobotsRules() {
            disallowedUrls = new ArrayList<String>();
            ready = false;
        }
    }

    public boolean isUrlAllowed(String url) {
        this.putifAbsentRules(url);
        String hostName = null;
        try {
            hostName = new URL(url).getHost();
        } catch (MalformedURLException e) {
            return false;
        }

        List<String> disallowedUrls = this.concMap.get(hostName).disallowedUrls;
        for (String disallowdUrl : disallowedUrls) {
            // match
            Pattern p = Pattern.compile(disallowdUrl);//. represents single character
            Matcher m = p.matcher(url);
            if (m.find()) {
                LogOutput.printMessage("URL disallowed By robots.txt : " + url);
                return false;
            }
        }
        return true;
    }

    private void putifAbsentRules(String url) {
        String hostName = null;
        try {
            hostName = new URL(url).getHost();
        } catch (MalformedURLException e1) {
            //e1.printStackTrace();
        }

        if (hostName == null) {
            return;
        }

        RobotsRules rules = this.concMap.putIfAbsent(hostName, new RobotsRules());

        if (rules == null) {
            LogOutput.printMessage("Host : " + hostName + " first time prepare robots.txt");
            this.update(hostName, this.parseRobotsFile(this.readRobotsFile(url)));
            return;
        }

        synchronized (rules) {
            while (!rules.ready) {
                try {
                    LogOutput.printMessage("Host : " + hostName + " Waiting for robots.txt file");
                    rules.wait();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    public List<String> readRobotsFile(String url) {
        List<String> lines = new ArrayList<String>();
        try {
            URL ur = new URL(url);
            url = ur.getProtocol() + "://" + ur.getHost() + "/robots.txt";
        } catch (MalformedURLException e1) {
            return lines;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (MalformedURLException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        return lines;
    }

    public List<String> parseRobotsFile(List<String> fileLines) {
        List<String> disallowedUrls = new ArrayList<String>();
        String userAgent = "";
        for (String line : fileLines) {
            line = line.toLowerCase();
            if (line.startsWith("user-agent")) {
                userAgent = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.startsWith("disallow")) {
                if (userAgent.equals(Constants.AGENT)) {
                    disallowedUrls.add(this.preparePattern(line.substring(line.indexOf(":") + 1).trim()));
                }
            }
        }
        return disallowedUrls;
    }

    public String preparePattern(String p) {
        p = p.replaceAll("\\.", "\\\\.");
        p = p.replaceAll("\\?", "[?]"); // match "?" mark.
        p = p.replaceAll("\\*", ".*"); // if "*" match any sequence of characters.
        p = p.replaceAll("\\{", "%7B");
        p = p.replaceAll("\\}", "%7D");
        return p;
    }

    private void update(String hostName, List<String> disallowedUrls) {
        RobotsRules rules = this.concMap.get(hostName);
        synchronized (rules) {
            rules.ready = true;
            rules.disallowedUrls = disallowedUrls;
            rules.notifyAll();
        }
    }
}
