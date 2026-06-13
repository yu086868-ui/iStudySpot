package com.ycyu.istudyspotbackend.agent.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class AgentReplyFormatter {
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.*?)\\*\\*");
    private static final Pattern INLINE_CODE = Pattern.compile("`([^`]*)`");
    private static final Pattern HEADING = Pattern.compile("^#{1,6}\\s+");
    private static final Pattern BULLET = Pattern.compile("^\\s*[-*+]\\s+");
    private static final Pattern NUMBERED = Pattern.compile("^\\s*\\d+[.)]\\s+");

    private AgentReplyFormatter() {
    }

    static String toPlainText(String markdown) {
        if (markdown == null) {
            return "";
        }
        String normalized = markdown.replace("\r\n", "\n").replace('\r', '\n');
        List<String> lines = new ArrayList<>();
        for (String rawLine : normalized.split("\n")) {
            String line = cleanInline(rawLine);
            line = HEADING.matcher(line).replaceFirst("");
            line = BULLET.matcher(line).replaceFirst("");
            line = NUMBERED.matcher(line).replaceFirst("");
            lines.add(line.trim());
        }
        return String.join("\n", lines).replaceAll("\\n{3,}", "\n\n").trim();
    }

    static List<AgentReplyBlock> toBlocks(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return List.of();
        }

        List<AgentReplyBlock> blocks = new ArrayList<>();
        List<String> paragraph = new ArrayList<>();
        List<String> bullets = new ArrayList<>();
        List<String> numbers = new ArrayList<>();

        String normalized = markdown.replace("\r\n", "\n").replace('\r', '\n');
        for (String rawLine : normalized.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                flushParagraph(blocks, paragraph);
                flushBullets(blocks, bullets);
                flushNumbers(blocks, numbers);
                continue;
            }

            if (BULLET.matcher(line).find()) {
                flushParagraph(blocks, paragraph);
                flushNumbers(blocks, numbers);
                bullets.add(cleanInline(BULLET.matcher(line).replaceFirst("")).trim());
                continue;
            }

            if (NUMBERED.matcher(line).find()) {
                flushParagraph(blocks, paragraph);
                flushBullets(blocks, bullets);
                numbers.add(cleanInline(NUMBERED.matcher(line).replaceFirst("")).trim());
                continue;
            }

            flushBullets(blocks, bullets);
            flushNumbers(blocks, numbers);
            paragraph.add(cleanInline(HEADING.matcher(line).replaceFirst("")).trim());
        }

        flushParagraph(blocks, paragraph);
        flushBullets(blocks, bullets);
        flushNumbers(blocks, numbers);
        return List.copyOf(blocks);
    }

    private static String cleanInline(String value) {
        String cleaned = BOLD.matcher(value).replaceAll("$1");
        cleaned = INLINE_CODE.matcher(cleaned).replaceAll("$1");
        return cleaned
                .replace("__", "")
                .replace("~~", "")
                .replaceAll("\\[(.*?)]\\((.*?)\\)", "$1");
    }

    private static void flushParagraph(List<AgentReplyBlock> blocks, List<String> paragraph) {
        if (paragraph.isEmpty()) {
            return;
        }
        String text = String.join("\n", paragraph).trim();
        if (!text.isEmpty()) {
            blocks.add(AgentReplyBlock.paragraph(text));
        }
        paragraph.clear();
    }

    private static void flushBullets(List<AgentReplyBlock> blocks, List<String> bullets) {
        if (!bullets.isEmpty()) {
            blocks.add(AgentReplyBlock.bullet(new ArrayList<>(bullets)));
            bullets.clear();
        }
    }

    private static void flushNumbers(List<AgentReplyBlock> blocks, List<String> numbers) {
        if (!numbers.isEmpty()) {
            blocks.add(AgentReplyBlock.numbered(new ArrayList<>(numbers)));
            numbers.clear();
        }
    }
}
