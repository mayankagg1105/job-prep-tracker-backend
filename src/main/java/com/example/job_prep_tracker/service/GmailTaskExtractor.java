package com.example.job_prep_tracker.service;

import com.example.job_prep_tracker.entity.Task;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GmailTaskExtractor {

    private static final Pattern INTERVIEW_PATTERN =
            Pattern.compile("interview|screening|call|meeting", Pattern.CASE_INSENSITIVE);

    private static final Pattern TEST_PATTERN =
            Pattern.compile("test|assessment|coding challenge|assignment", Pattern.CASE_INSENSITIVE);

    private static final Pattern DEADLINE_PATTERN =
            Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|" +
                            "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{1,2})",
                    Pattern.CASE_INSENSITIVE);

    public List<Task> extractTasks(String accessToken, UUID userId) {
        try {
            Gmail service = buildGmailService(accessToken);

            // Query for relevant emails
            String query = "subject:(interview OR test OR assessment OR assignment OR task) " +
                    "AND is:unread " +
                    "AND newer_than:30d";

            ListMessagesResponse response = service.users().messages()
                    .list("me")
                    .setQ(query)
                    .setMaxResults(50L)
                    .execute();

            if (response.getMessages() == null) {
                return new ArrayList<>();
            }

            return response.getMessages().stream()
                    .map(msg -> extractTaskFromMessage(service, msg.getId(), userId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract tasks from Gmail", e);
        }
    }

    private Gmail buildGmailService(String accessToken) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.create(
                new AccessToken(accessToken, null)
        );

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("Job Prep Tracker")
                .build();
    }

    private Task extractTaskFromMessage(Gmail service, String messageId, UUID userId) {
        try {
            Message message = service.users().messages()
                    .get("me", messageId)
                    .setFormat("full")
                    .execute();

            String subject = getHeader(message, "Subject");
            String from = getHeader(message, "From");
            String body = getMessageBody(message);

            // Extract task type
            String taskType = determineTaskType(subject, body);
            if (taskType == null) return null;

            // Extract company
            String company = extractCompany(from);

            // Extract deadline
            LocalDateTime deadline = extractDeadline(subject, body);

            // Extract meeting link
            String meetingLink = extractMeetingLink(body);

            // Build Task entity
            Task task = new Task();
            task.setUserId(userId);
            task.setGmailMessageId(messageId);
            task.setCompanyName(company);
            task.setTaskType(taskType);
            task.setSubject(subject);
            task.setDeadline(deadline);
            task.setMeetingLink(meetingLink);
            task.setStatus("PENDING");
            task.setPriority(calculatePriority(deadline));

            String snippet = body.replaceAll("\\s+", " ").trim();
            task.setEmailSnippet(snippet.substring(0, Math.min(200, snippet.length())));

            return task;

        } catch (Exception e) {
            System.err.println("Failed to extract task from message: " + messageId);
            return null;
        }
    }

    private static String getHeader(Message message, String name) {
        if (message.getPayload() == null || message.getPayload().getHeaders() == null) {
            return "";
        }

        return message.getPayload()
                .getHeaders()
                .stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse("");
    }

    private static String getMessageBody(Message message) {
        if (message.getPayload() == null) {
            return "";
        }

        if (message.getPayload().getBody() != null &&
                message.getPayload().getBody().getData() != null) {
            return new String(
                    Base64.getUrlDecoder().decode(
                            message.getPayload().getBody().getData()
                    )
            );
        }

        // Check parts for text/plain
        if (message.getPayload().getParts() != null) {
            for (var part : message.getPayload().getParts()) {
                if ("text/plain".equals(part.getMimeType())) {
                    if (part.getBody() != null && part.getBody().getData() != null) {
                        return new String(
                                Base64.getUrlDecoder().decode(part.getBody().getData())
                        );
                    }
                }
            }
        }

        return "";
    }

    private String determineTaskType(String subject, String body) {
        String combined = (subject + " " + body).toLowerCase();

        if (INTERVIEW_PATTERN.matcher(combined).find()) {
            return "INTERVIEW";
        }
        if (TEST_PATTERN.matcher(combined).find()) {
            return "TEST";
        }
        return null;
    }

    private LocalDateTime extractDeadline(String subject, String body) {
        Matcher matcher = DEADLINE_PATTERN.matcher(body);

        if (matcher.find()) {
            String dateStr = matcher.group(1);

            // Try to parse month + day format
            Pattern monthDayPattern = Pattern.compile(
                    "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]* (\\d{1,2})",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher monthMatcher = monthDayPattern.matcher(dateStr);
            if (monthMatcher.find()) {
                String month = monthMatcher.group(1);
                int day = Integer.parseInt(monthMatcher.group(2));
                int monthNum = getMonthNumber(month);
                int year = LocalDateTime.now().getYear();

                return LocalDateTime.of(year, monthNum, day, 14, 0);
            }

            // Try numeric date format
            Pattern numericPattern = Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})");
            Matcher numericMatcher = numericPattern.matcher(dateStr);

            if (numericMatcher.find()) {
                int month = Integer.parseInt(numericMatcher.group(1));
                int day = Integer.parseInt(numericMatcher.group(2));
                int year = Integer.parseInt(numericMatcher.group(3));

                if (year < 100) year += 2000;

                return LocalDateTime.of(year, month, day, 14, 0);
            }
        }

        // Default: 7 days from now
        return LocalDateTime.now().plusDays(7);
    }

    private String extractMeetingLink(String body) {
        Pattern linkPattern = Pattern.compile(
                "(https?://[^\\s]+(?:zoom\\.us|meet\\.google\\.com|teams\\.microsoft\\.com|webex\\.com)[^\\s]*)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = linkPattern.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractCompany(String fromEmail) {
        try {
            // Extract domain: recruiter@amazon.com -> amazon
            String[] parts = fromEmail.split("@");
            if (parts.length < 2) return "Unknown";

            String domain = parts[1];
            String company = domain.split("\\.")[0];

            // Capitalize first letter
            return company.substring(0, 1).toUpperCase() + company.substring(1);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String calculatePriority(LocalDateTime deadline) {
        long hoursUntil = ChronoUnit.HOURS.between(LocalDateTime.now(), deadline);

        if (hoursUntil < 24) return "HIGH";
        if (hoursUntil < 72) return "MEDIUM";
        return "LOW";
    }

    private int getMonthNumber(String month) {
        month = month.toLowerCase();
        if (month.startsWith("jan")) return 1;
        if (month.startsWith("feb")) return 2;
        if (month.startsWith("mar")) return 3;
        if (month.startsWith("apr")) return 4;
        if (month.startsWith("may")) return 5;
        if (month.startsWith("jun")) return 6;
        if (month.startsWith("jul")) return 7;
        if (month.startsWith("aug")) return 8;
        if (month.startsWith("sep")) return 9;
        if (month.startsWith("oct")) return 10;
        if (month.startsWith("nov")) return 11;
        if (month.startsWith("dec")) return 12;
        return 1;
    }
}