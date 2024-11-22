package com.web.forumSocialX.statistics;


import com.web.forumSocialX.category.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RestController

@RequestMapping(path ="/statistics")
@RequiredArgsConstructor

public class StatisticsController {

    private final  StatisticsService statisticsService;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  /*  @GetMapping("/postes/period")
    public ResponseEntity<Map<String, Long>> getPostesStatistics(@RequestParam("period") String period) {
        Date startDate = statisticsService.getStartDateForPeriod(period);
        Map<Category, Long> stats = statisticsService.getPostesStatisticsByPeriod(startDate);

        // Convertir Map<Category, Long> en Map<String, Long>
        Map<String, Long> stringKeyStats = stats.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(), // Utiliser le nom de l'enum comme clé
                        Map.Entry::getValue
                ));

        return ResponseEntity.ok(stringKeyStats);
    }*/

    @GetMapping("/postes/period")
    public ResponseEntity<Map<Category, Long>> getPostesStatistics(@RequestParam("period") String period) {
        Date startDate = statisticsService.getStartDateForPeriod(period);
        Map<Category, Long> stats = statisticsService.getPostesStatisticsByPeriod(startDate);
        return ResponseEntity.ok(stats);
    }



    @GetMapping("/postes")
    public ResponseEntity<Map<String, Map<Category, Long>>> getStatistics(
            @RequestParam String category,
            @RequestParam String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date selectedDate) {

        // Appel du service pour obtenir les statistiques basées sur les paramètres fournis
        Map<String, Map<Category, Long>> statistics = statisticsService.getPostesStatistics(category, period, selectedDate);

        // Retourner les statistiques dans la réponse HTTP
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/comments")
    public ResponseEntity<Map<String, Map<Category, Long>>> getCommentsStatistics(
            @RequestParam String category,
            @RequestParam String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date selectedDate) {

        // Appel du service pour obtenir les statistiques basées sur les paramètres fournis
        Map<String, Map<Category, Long>> statistics = statisticsService.getCommentsStatistics(category, period, selectedDate);

        // Retourner les statistiques dans la réponse HTTP
        return ResponseEntity.ok(statistics);
    }
    @GetMapping("/groupes")
    public ResponseEntity<Map<String, Map<Category, Long>>> getGroupesStatistics(
            @RequestParam String category,
            @RequestParam String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date selectedDate) {

        // Appel du service pour obtenir les statistiques basées sur les paramètres fournis
        Map<String, Map<Category, Long>> statistics = statisticsService.getGroupesStatistics(category, period, selectedDate);

        // Retourner les statistiques dans la réponse HTTP
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/postes/percentage")
    public ResponseEntity <Map<String, Object>> getPostesStatistics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
         Map<String, Object> statistics = statisticsService.getPostesStatisticsByPeriod(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/comments/percentage")
    public ResponseEntity<Map<String, Object>> getCommentsStatistics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        Map<String, Object>statistics = statisticsService.getCommentsStatisticsByPeriod(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/groups/percentage")
    public ResponseEntity<Map<String, Object>> gGroupsStatistics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        Map<String, Object> statistics = statisticsService.getGroupsStatisticsByPeriod(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/users/percentage")
    public ResponseEntity<List<Map<String, Object>>> getUserStatistics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam("granularity") String granularity) {

        List<Map<String, Object>> statistics = statisticsService.getUserStatisticsByGranularity(startDate, endDate, granularity);

        return ResponseEntity.ok(statistics);
    }


    @GetMapping("/users/status/percentage")
    public ResponseEntity<Map<String, Double>> getUserStatusPercentage() {
        Map<String, Double> percentages = statisticsService.calculateUserStatusPercentage();
        return ResponseEntity.ok(percentages);
    }
}
