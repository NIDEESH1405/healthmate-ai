package com.healthmate.ai.service;

import com.healthmate.ai.model.KbTopic;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent 5: Hospital Info.
 * Provides KB-grounded Q&A (restricting the LLM strictly to retrieved topic content,
 * so it cannot hallucinate hospital-specific facts) and full KB directory browsing.
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

    public QaResult ask(String question) {
        List<KbTopic> allTopics = knowledgeBaseService.getAllTopics();
        List<KbTopic> topMatches = retrievalService.retrieveTopTopics(question, 3);

        QaResult result = new QaResult();
        // Show the most relevant topics as "sources" for transparency, but fall back to
        // showing all topics if retrieval didn't find a clear match, since the LLM still
        // has access to the complete knowledge base below.
        result.sources = topMatches.isEmpty() ? allTopics : topMatches;

        String context = allTopics.stream()
                .map(t -> "Topic: " + t.getTitle() + "\n" + t.getContent())
                .collect(Collectors.joining("\n\n"));

        String prompt = "Complete hospital knowledge base:\n\n" + context
                + "\n\nPatient question: " + question
                + "\n\nAnswer as thoroughly and completely as possible using ONLY the information in the knowledge "
                + "base above. Include every relevant detail that applies to the question — do not give a partial "
                + "or overly brief answer if more relevant information is available in the context. "
                + "If the knowledge base doesn't fully answer the question, say so plainly and suggest contacting "
                + "the front desk, rather than inventing details.";

        result.answer = groqService.singlePrompt(
                "You are " + knowledgeBaseService.getHospitalName() + "'s informational front-desk assistant. "
                        + "You answer only using the provided knowledge base context — never invent hospital-specific "
                        + "facts (like names, hours, or prices) that aren't in the context. You never give medical advice. "
                        + "Always be as complete and thorough as the available context allows.",
                prompt
        );
        return result;
    }

    public List<KbTopic> allTopics() {
        return knowledgeBaseService.getAllTopics();
    }

    public String hospitalName() {
        return knowledgeBaseService.getHospitalName();
    }
}
