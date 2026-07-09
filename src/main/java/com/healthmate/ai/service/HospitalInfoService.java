package com.healthmate.ai.service;

import com.healthmate.ai.model.ChatMessage;
import com.healthmate.ai.model.KbTopic;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent 5: Hospital Info.
 * Provides KB-grounded Q&A (restricting the LLM strictly to the full knowledge base content,
 * so it cannot hallucinate hospital-specific facts) with real conversational memory — follow-up
 * questions are answered in context of what was already discussed, like a genuine assistant
 * rather than a series of disconnected one-shot lookups — plus full KB directory browsing.
 */
@Service
public class HospitalInfoService {

    private final RetrievalService retrievalService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final GroqService groqService;

    public HospitalInfoService(RetrievalService retrievalService,
                                KnowledgeBaseService knowledgeBaseService,
                                GroqService groqService) {
        this.retrievalService = retrievalService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.groqService = groqService;
    }

    public static class QaResult {
        public String answer;
        public List<KbTopic> sources;
    }

    /**
     * Answers a question with conversational memory: prior turns in {@code history} are included
     * so follow-up questions ("what about on weekends?") are understood in context, without the
     * user needing to repeat themselves. {@code history} is mutated in place with the new turn.
     */
    public QaResult ask(String question, List<ChatMessage> history) {
        List<KbTopic> allTopics = knowledgeBaseService.getAllTopics();
        List<KbTopic> topMatches = retrievalService.retrieveTopTopics(question, 4);

        QaResult result = new QaResult();
        result.sources = topMatches.isEmpty() ? allTopics : topMatches;

        String context = allTopics.stream()
                .map(t -> "Topic: " + t.getTitle() + "\n" + t.getContent())
                .collect(Collectors.joining("\n\n"));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(
                "You are " + knowledgeBaseService.getHospitalName() + "'s informational front-desk assistant. "
                        + "You answer only using the knowledge base context provided below — never invent hospital-specific "
                        + "facts (names, hours, prices, phone numbers) that aren't in the context. You never give medical advice.\n\n"
                        + "Be specific and concrete: cite the actual names, times, and figures from the knowledge base rather "
                        + "than vague statements — e.g. say 'OPD runs 8 AM to 8 PM Monday to Saturday' rather than 'we have "
                        + "standard hours'. When a question touches multiple topics (e.g. booking with a specialist AND "
                        + "parking), synthesize a single coherent answer drawing from all relevant topics rather than "
                        + "answering only the first part. Where genuinely useful, proactively mention one closely related "
                        + "detail the patient likely also needs (e.g. if asked about a department, briefly mention how to "
                        + "book) — but don't pad the answer with irrelevant information. "
                        + "This is a continuing conversation — use the prior turns below to understand follow-up questions "
                        + "in context (e.g. 'what about weekends?' refers back to whatever was just discussed). "
                        + "If the knowledge base doesn't fully answer the question, say so plainly and suggest contacting "
                        + "the front desk, rather than inventing details.\n\n"
                        + "KNOWLEDGE BASE:\n" + context));
        messages.addAll(history);
        messages.add(ChatMessage.user(question));

        String answer = groqService.chat(messages);

        history.add(ChatMessage.user(question));
        history.add(ChatMessage.assistant(answer));

        result.answer = answer;
        return result;
    }

    public List<KbTopic> allTopics() {
        return knowledgeBaseService.getAllTopics();
    }

    public String hospitalName() {
        return knowledgeBaseService.getHospitalName();
    }
}
