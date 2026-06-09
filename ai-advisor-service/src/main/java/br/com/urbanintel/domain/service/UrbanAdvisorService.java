package br.com.urbanintel.domain.service;

import br.com.urbanintel.domain.port.in.UrbanAdvisorUseCase;
import br.com.urbanintel.tool.UrbanIntelTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrbanAdvisorService implements UrbanAdvisorUseCase {

    private final ChatClient chatClient;
    private final UrbanIntelTools urbanTools;

    private static final List<RagDocument> RAG_KNOWLEDGE_BASE = List.of(
        new RagDocument("linha10 cptm abc atraso chuva",
            "Linha 10 Turquesa (CPTM): histórico CRÍTICO em chuvas > 8mm. Trecho ABC-Brás frequentemente interrompido. Em 2024 registrou 23 ocorrências em dias de chuva forte."),
        new RagDocument("linha11 coral zona leste atraso chuva",
            "Linha 11 Coral (CPTM): risco ALTO em chuvas > 10mm. Trecho Guaianazes-Luz sofre alagamentos na região da Penha. Média de 55% de atraso em dias chuvosos."),
        new RagDocument("linha9 esmeralda osasco grajaú atraso",
            "Linha 9 Esmeralda (CPTM): risco ALTO em chuvas > 10mm. Trecho Osasco-Grajaú tem histórico de lentidão. Recomenda-se sair 40min mais cedo em dias de chuva."),
        new RagDocument("alagamento zona leste são paulo enchente",
            "Regiões com maior histórico de alagamento em SP: Marginal Tietê, Viaduto do Glicério, Av. do Estado, bairros do ABC. Em chuvas > 20mm evite vias de baixada."),
        new RagDocument("uv protetor solar calor são paulo verão",
            "São Paulo: UV alto entre outubro e março. Índice UV acima de 8 requer protetor FPS 50+. Evitar exposição entre 10h-16h. Pico de UV ocorre por volta das 13h."),
        new RagDocument("guarda chuva chuva previsão levar recomendação",
            "Recomendação: levar guarda-chuva quando precipitação prevista > 5mm ou condição RAIN/STORM. Em chuvas > 15mm prefira capa de chuva. Evite sair nas primeiras horas de chuva forte."),
        new RagDocument("trânsito rush horário pico são paulo",
            "Horário de pico em SP: manhã 7h-9h e tarde 17h-19h. Em dias de chuva o tempo de deslocamento aumenta 40-60%. Recomenda-se sair 30-45min mais cedo ou usar metrô.")
    );

    private static final String SYSTEM_PROMPT = """
        Você é o Assistente Urbano da plataforma Urban Intel para São Paulo.
        Responda SEMPRE em português brasileiro, de forma clara e prática.
        Use as ferramentas disponíveis para buscar dados reais antes de responder.
        Mencione o nível de risco (BAIXO, MÉDIO, ALTO, CRÍTICO) quando relevante.
        Seja específico: mencione linhas de metrô, bairros e horários.
        
        Contexto histórico relevante (base de conhecimento RAG):
        %s
        
        Data/hora atual: %s
        """;

    @Override
    public String ask(String question, String city) {
        log.info("Consulta ao advisor: {}", question);

        String ragContext = retrieveRelevantContext(question);
        log.info("RAG: {} documentos relevantes encontrados", ragContext.isBlank() ? 0 : ragContext.split("\n-").length);

        String system = String.format(SYSTEM_PROMPT, ragContext,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        return chatClient.prompt()
                .system(system)
                .user(question)
                .tools(urbanTools)
                .call()
                .content();
    }

    @Override
    public DailySummary generateDailySummary(String city) {
        String ragContext = retrieveRelevantContext("clima chuva transporte " + city);
        String system = String.format(SYSTEM_PROMPT, ragContext,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        String content = chatClient.prompt()
                .system(system)
                .user(String.format("""
                    Gere um resumo do dia para %s com:
                    1. Condição climática e riscos
                    2. Impactos no transporte público
                    3. Top 3 recomendações práticas
                    4. Score do dia de 0 a 10
                    Use as ferramentas para buscar dados atuais.
                    """, city))
                .tools(urbanTools)
                .call()
                .content();

        return new DailySummary(city, content, LocalDateTime.now());
    }

    @Override
    public String analyzeTransitLine(String lineName, String city) {
        String ragContext = retrieveRelevantContext(lineName + " chuva atraso");
        String system = String.format(SYSTEM_PROMPT, ragContext,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        return chatClient.prompt()
                .system(system)
                .user(String.format(
                    "Analise o risco climático na %s em %s agora. " +
                    "Use as ferramentas para verificar clima e alertas ativos. " +
                    "Diga se o passageiro deve evitar essa linha e por quê.", lineName, city))
                .tools(urbanTools)
                .call()
                .content();
    }

    private String retrieveRelevantContext(String query) {
        String queryLower = query.toLowerCase();
        StringBuilder context = new StringBuilder();

        RAG_KNOWLEDGE_BASE.stream()
                .filter(doc -> {
                    String[] keywords = doc.keywords().split(" ");
                    int matches = 0;
                    for (String kw : keywords) {
                        if (queryLower.contains(kw)) matches++;
                    }
                    return matches >= 1;
                })
                .limit(3)
                .forEach(doc -> context.append("- ").append(doc.content()).append("\n"));

        return context.toString();
    }

    private record RagDocument(String keywords, String content) {}
}
