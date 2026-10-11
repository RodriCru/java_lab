package com.lrch.javalab.questions;

import com.lrch.javalab.common.ApiException;
import com.lrch.javalab.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class QuestionService {

    private final QuestionBank bank;
    private final QuestionAttemptRepository attempts;

    public QuestionService(QuestionBank bank, QuestionAttemptRepository attempts) {
        this.bank = bank;
        this.attempts = attempts;
    }

    /** Estado personal: PENDING (sin responder), CORRECT (alguna vez acertada) o FAILED (solo falladas). */
    private static String estado(String id, Set<String> respondidas, Set<String> acertadas) {
        if (acertadas.contains(id)) {
            return "CORRECT";
        }
        return respondidas.contains(id) ? "FAILED" : "PENDING";
    }

    public PageResult<QuestionView> listar(Long userId, String topic, String difficulty, String status,
                                           String search, boolean aleatorio, int page, int perPage) {
        Set<String> respondidas = new HashSet<>(attempts.findIdsRespondidas(userId));
        Set<String> acertadas = new HashSet<>(attempts.findIdsAcertadas(userId));
        String estadoPedido = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        String texto = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        List<QuestionView> filtradas = bank.all().stream()
                .filter(q -> topic == null || topic.isBlank() || topic.equalsIgnoreCase(q.topic()))
                .filter(q -> difficulty == null || difficulty.isBlank() || difficulty.equalsIgnoreCase(q.difficulty()))
                .filter(q -> texto.isEmpty() || q.question().toLowerCase(Locale.ROOT).contains(texto))
                .filter(q -> estadoPedido.isEmpty() || estadoPedido.equals(estado(q.id(), respondidas, acertadas)))
                .map(q -> q.view(estado(q.id(), respondidas, acertadas)))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        if (aleatorio) {
            java.util.Collections.shuffle(filtradas);
        }

        long total = filtradas.size();
        int desde = Math.min((page - 1) * perPage, filtradas.size());
        int hasta = Math.min(desde + perPage, filtradas.size());
        return new PageResult<>(List.copyOf(filtradas.subList(desde, hasta)), total, page, perPage);
    }

    public List<TopicSummary> temas(Long userId) {
        Set<String> respondidas = new HashSet<>(attempts.findIdsRespondidas(userId));
        Set<String> acertadas = new HashSet<>(attempts.findIdsAcertadas(userId));
        Map<String, int[]> porTema = new LinkedHashMap<>();
        bank.all().stream().map(Question::topic).sorted().forEach(t -> porTema.putIfAbsent(t, new int[3]));
        for (Question q : bank.all()) {
            int[] c = porTema.get(q.topic());
            c[0]++;
            if (respondidas.contains(q.id())) {
                c[1]++;
            }
            if (acertadas.contains(q.id())) {
                c[2]++;
            }
        }
        return porTema.entrySet().stream()
                .map(e -> new TopicSummary(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .toList();
    }

    @Transactional
    public AnswerResponse responder(Long userId, String id, Integer elegida) {
        Question q = bank.byId(id).orElseThrow(() -> ApiException.notFound("PREGUNTA_NO_ENCONTRADA",
                "No existe la pregunta «" + id + "»."));
        if (elegida == null || elegida < 0 || elegida >= q.options().size()) {
            throw ApiException.badRequest("RESPUESTA_INVALIDA",
                    "Elige una opción entre 0 y " + (q.options().size() - 1) + ".");
        }
        boolean correcta = elegida == q.correctIndex();
        attempts.save(new QuestionAttempt(userId, id, elegida, correcta));
        return new AnswerResponse(correcta, q.correctIndex(), q.explanation());
    }
}
