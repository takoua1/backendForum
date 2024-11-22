package com.web.forumSocialX.statistics;


import com.web.forumSocialX.category.Category;
import com.web.forumSocialX.comment.Comment;
import com.web.forumSocialX.comment.CommentRepository;
import com.web.forumSocialX.groupe.Groupe;
import com.web.forumSocialX.groupe.GroupeRepository;
import com.web.forumSocialX.poste.Poste;
import com.web.forumSocialX.poste.PosteRepository;
import com.web.forumSocialX.user.Role;
import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import com.web.forumSocialX.user.Status;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StatisticsService {
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
    private static final SimpleDateFormat WEEK_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRENCH);
    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.FRENCH);

    private static final List<String> MONTH_NAMES = Arrays.asList(
            "Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jui", "Aoû", "Sep", "Oct", "Nov", "Déc"
    );

    private static final List<String> DAY_NAMES = Arrays.asList(
            "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"
    );


    private final PosteRepository posteRepository;
 private final CommentRepository commentRepository;
    private final GroupeRepository groupeRepository;
    private final  UserRepository userRepository;
    /* public Map<Category, Long> getPostesStatisticsByPeriod(Date startDate) {
         return posteRepository.countPostesByCategoryAndPeriod(startDate);
     }*/
    public Map<Category, Long> getPostesStatisticsByPeriod(Date startDate) {
        List<Poste> postes = posteRepository.findByDateCreateAfterAndEnabledTrue(startDate);

        Map<Category, Long> statistics = Arrays.stream(Category.values())
                .collect(Collectors.toMap(Function.identity(), c -> 0L));

        // Mettre à jour les statistiques en fonction des postes
        for (Poste poste : postes) {
            Category category = poste.getCategory();
            statistics.put(category, statistics.get(category) + 1); // Compte les postes par catégorie
        }

        return statistics;
    }

    public Map<String, Object> getPostesStatisticsByPeriod(Date startDate, Date endDate) {
        List<Poste> postes = posteRepository.findByDateCreateBetweenAndEnabledTrue(startDate, endDate);

        // Calculer le nombre total de postes dans la période
        long totalPostes = postes.size();

        // Initialiser les statistiques avec des valeurs de 0
        Map<Category, Long> countStatistics = Arrays.stream(Category.values())
                .collect(Collectors.toMap(Function.identity(), c -> 0L));

        // Mettre à jour les statistiques en fonction des postes
        for (Poste poste : postes) {
            Category category = poste.getCategory();
            countStatistics.put(category, countStatistics.get(category) + 1);
        }

        // Calculer les pourcentages
        Map<Category, Double> percentageStatistics = countStatistics.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (totalPostes > 0) ? (entry.getValue() * 100.0 / totalPostes) : 0.0
                ));

        // Créer une réponse qui inclut les pourcentages et le total des postes
        Map<String, Object> response = new HashMap<>();
        response.put("percentages", percentageStatistics);
        response.put("totalPostes", totalPostes);

        return response;
    }
    public Map<String, Map<Category, Long>> getPostesStatistics(String category, String period, Date selectedDate) {
        Date startDate;
        Date endDate;

        switch (period) {
            case "year":
                startDate = getStartDateForYear(selectedDate);
                endDate =DateUtils.addMonths(startDate, 11); // Date actuelle
                return getMonthlyStatistics(startDate, endDate, category);

            case "month":
                startDate = getStartDateForMonth(selectedDate);
                endDate = DateUtils.addMonths(startDate, 1); // Fin du mois
                return getWeeklyStatistics(startDate, endDate, category);

            case "week":
                startDate = getStartDateForWeek(selectedDate);
                endDate = DateUtils.addDays(startDate, 6); // Fin de la semaine
                return getDailyStatistics(startDate, endDate, category);

            case "day":
                startDate = getStartDateForDay(selectedDate);
                endDate = selectedDate; // Jusqu'à la date actuelle
                return getHourlyStatistics(startDate, endDate, category);

            default:
                throw new IllegalArgumentException("Période non reconnue: " + period);
        }
    }

    private Map<String, Map<Category, Long>> getMonthlyStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> monthlyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);  // Commence à partir de startDate

        while (cal.getTime().before(endDate) || cal.getTime().equals(endDate)) {
            Date periodStart = cal.getTime();

            // Avancer d'un mois
            cal.add(Calendar.MONTH, 1);

            // Ajuster la fin de la période
            Date periodEnd = cal.getTime();

            // Si periodEnd dépasse endDate, on limite la période à endDate
            if (periodEnd.after(endDate)) {
                periodEnd = endDate;
            }

            // Formater le nom du mois et l'année pour la clé
            String key = MONTH_NAMES.get(cal.get(Calendar.MONTH) == 0 ? 11 : cal.get(Calendar.MONTH) - 1)
                    + " " + cal.get(Calendar.YEAR);

            // Ajouter les statistiques pour le mois courant
            monthlyStats.put(key, getStatisticsByPeriod(periodStart, periodEnd, category, PeriodType.WEEK));
        }

        return monthlyStats;
    }


    private Map<String, Map<Category, Long>> getWeeklyStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> weeklyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate); // Utilise startDate directement comme début

        while (cal.getTime().before(endDate)) {
            Date periodStart = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, 6); // Fin de la semaine à partir de startDate
            Date periodEnd = cal.getTime();

            // Limiter la fin de la semaine si nécessaire
            if (periodEnd.after(endDate)) {
                periodEnd = endDate;
            }

            String key = formatPeriod(periodStart, periodEnd);
            weeklyStats.put(key, getStatisticsByPeriod(periodStart, periodEnd, category, PeriodType.DAY));

            // Passer à la semaine suivante
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return weeklyStats;
    }
    private String formatPeriod(Date periodStart, Date periodEnd) {
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(periodStart);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(periodEnd);

        String startDay = DAY_NAMES.get(calStart.get(Calendar.DAY_OF_WEEK) - 1);
        String startMonth = MONTH_NAMES.get(calStart.get(Calendar.MONTH));
        String endDay = DAY_NAMES.get(calEnd.get(Calendar.DAY_OF_WEEK) - 1);
        String endMonth = MONTH_NAMES.get(calEnd.get(Calendar.MONTH));

        // Pour les heures
        if (calStart.get(Calendar.DAY_OF_YEAR) == calEnd.get(Calendar.DAY_OF_YEAR)) {
            return String.format("%s %d %s %02d:%02d - %02d:%02d",
                    startDay, calStart.get(Calendar.DAY_OF_MONTH), startMonth,
                    calStart.get(Calendar.HOUR_OF_DAY), calStart.get(Calendar.MINUTE),
                    calEnd.get(Calendar.HOUR_OF_DAY), calEnd.get(Calendar.MINUTE));
        } else {
            return String.format("%s %d %s %02d:%02d - %s %d %s %02d:%02d",
                    startDay, calStart.get(Calendar.DAY_OF_MONTH), startMonth,
                    calStart.get(Calendar.HOUR_OF_DAY), calStart.get(Calendar.MINUTE),
                    endDay, calEnd.get(Calendar.DAY_OF_MONTH), endMonth,
                    calEnd.get(Calendar.HOUR_OF_DAY), calEnd.get(Calendar.MINUTE));
        }
    }
    private Map<String, Map<Category, Long>> getDailyStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> dailyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        System.out.println("Start Date: " + new SimpleDateFormat("yyyy-MM-dd").format(startDate));
        System.out.println("End Date: " + new SimpleDateFormat("yyyy-MM-dd").format(endDate));

        // Ajuster la startDate à 00:00:00
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0); // Minuit
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date periodStart = cal.getTime();
        System.out.println("Adjusted startDate (periodStart): " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(periodStart));

        // Ajuster la endDate à 23:59:59
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23); // 23h59
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date periodEnd = endCal.getTime();
        System.out.println("Adjusted endDate (periodEnd): " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(periodEnd));

        // Traitement des jours de la période
        while (periodStart.before(periodEnd)) {
            // Formatte la date pour la clé dans le map
            Calendar nextDayCal = Calendar.getInstance();
            nextDayCal.setTime(periodStart);
            nextDayCal.add(Calendar.DAY_OF_YEAR, 1); // Passe au jour suivant
            Date periodNext = nextDayCal.getTime();

            // Formater la date pour la clé dans le map
            String key = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRENCH).format(periodStart);
            System.out.println("Processing period: " + key + " to " + new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRENCH).format(periodNext));

            // Appeler la méthode qui récupère les statistiques pour ce jour (intervalle)
            dailyStats.put(key, getStatisticsByPeriod(periodStart, periodNext, category, PeriodType.HOUR));

            // Passe au jour suivant (réajuster periodStart)
            periodStart = periodNext;
            System.out.println("Next periodStart: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(periodStart));
        }

        return dailyStats;
    }


    private Map<String, Map<Category, Long>> getHourlyStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> hourlyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        // Récupère la date actuelle
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        // Si endDate est dans le futur, ajuste endDate à la date actuelle
        if (endDate.after(now.getTime())) {
            endDate = now.getTime();
        }

        // On fixe le calendrier à l'heure de début
        cal.setTime(startDate);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Vérifie si endDate est aujourd'hui
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        // Ajuste endCal si endDate est aujourd'hui
        if (endDate.equals(now.getTime())) {
            endCal.setTime(now.getTime());
        }

        // Tant que la date de début est avant la date de fin
        while (cal.getTime().before(endCal.getTime())) {
            Date periodStart = cal.getTime();  // Début de l'heure
            cal.add(Calendar.HOUR_OF_DAY, 1);  // On avance d'une heure
            Date periodEnd = cal.getTime();    // Fin de l'heure

            // Ajuste periodEnd si nécessaire
            if (periodEnd.after(endCal.getTime())) {
                periodEnd = endCal.getTime();
            }

            // Formatter pour l'affichage des heures
            String key = formatPeriod(periodStart, periodEnd);

            // Ajout des statistiques pour l'heure en cours
            hourlyStats.put(key, getStatisticsByPeriod(periodStart, periodEnd, category, PeriodType.MINUTE));

            // Si periodEnd est exactement endCal, on arrête la boucle
            if (periodEnd.equals(now.getTime())) {
                break;
            }
        }

        return hourlyStats;
    }



    private Map<Category, Long> getStatisticsByPeriod(Date startDate, Date endDate, String category, PeriodType periodType) {
        Map<Category, Long> statistics = new HashMap<>();

        if ("All".equalsIgnoreCase(category)) {
            // Récupérer les postes pour toutes les catégories entre les dates spécifiées
            List<Poste> postes = posteRepository.findByDateCreateBetweenAndEnabledTrue(startDate, endDate);

            // Calculer le nombre total de postes dans la période
            long totalPostes = postes.size();

            // Initialiser les statistiques avec des valeurs de 0 pour chaque catégorie
            Map<Category, Long> countStatistics = Arrays.stream(Category.values())
                    .collect(Collectors.toMap(Function.identity(), c -> 0L));

            // Mettre à jour les statistiques en fonction des postes
            for (Poste poste : postes) {
                Category postCategory = poste.getCategory();
                countStatistics.put(postCategory, countStatistics.get(postCategory) + 1);
            }

            statistics.putAll(countStatistics);
        } else {
            // Si une catégorie spécifique est fournie
            Category categoryEnum = Category.valueOf(category);

            // Récupérer les postes de la catégorie donnée entre les dates spécifiées
            List<Poste> posts = posteRepository.findByCategoryAndDateCreateBetweenAndEnabledTrue(categoryEnum, startDate, endDate);

            // Initialiser la statistique pour cette catégorie
            long count = posts.size();
            statistics.put(categoryEnum, count);
        }

        return statistics;
    }


    private Date getStartDateForYear(Date selectedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate); // Utilise la date sélectionnée
        cal.set(Calendar.HOUR_OF_DAY, 0); // Réinitialise l'heure à 00:00
        cal.set(Calendar.MINUTE, 0); // Réinitialise les minutes à 00
        cal.set(Calendar.SECOND, 0); // Réinitialise les secondes à 00
        cal.set(Calendar.MILLISECOND, 0); // Réinitialise les millisecondes à 00
        return cal.getTime();
    }

    private Date getStartDateForMonth(Date selectedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate); // Utilise la date sélectionnée
        cal.set(Calendar.HOUR_OF_DAY, 0); // Réinitialise l'heure à 00:00
        cal.set(Calendar.MINUTE, 0); // Réinitialise les minutes à 00
        cal.set(Calendar.SECOND, 0); // Réinitialise les secondes à 00
        cal.set(Calendar.MILLISECOND, 0); // Réinitialise les millisecondes à 00
        return cal.getTime(); // Retourne la date ajustée
    }

    private Date getStartDateForWeek(Date selectedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);

        // Réinitialiser les heures, minutes, secondes et millisecondes
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Retourner la date sélectionnée comme le début de la semaine
        return cal.getTime();
    }
    private Date getStartDateForDay(Date selectedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    public enum PeriodType {
       YEAR, MONTH, WEEK, DAY, HOUR, MINUTE
    }








    /*  private Date getStartDateForDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }*/

  public Date getStartDateForPeriod(String period) {
      Calendar calendar = Calendar.getInstance();
      switch (period.toLowerCase()) {
          case "day":
              // Début de la journée en cours
              calendar.set(Calendar.HOUR_OF_DAY, 0);
              calendar.set(Calendar.MINUTE, 0);
              calendar.set(Calendar.SECOND, 0);
              calendar.set(Calendar.MILLISECOND, 0);
              break;

          case "week":
              // Début de la semaine en cours (dimanche)
              calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
              calendar.set(Calendar.HOUR_OF_DAY, 0);
              calendar.set(Calendar.MINUTE, 0);
              calendar.set(Calendar.SECOND, 0);
              calendar.set(Calendar.MILLISECOND, 0);
              break;

          case "month":
              // Début du mois en cours
              calendar.set(Calendar.DAY_OF_MONTH, 1);
              calendar.set(Calendar.HOUR_OF_DAY, 0);
              calendar.set(Calendar.MINUTE, 0);
              calendar.set(Calendar.SECOND, 0);
              calendar.set(Calendar.MILLISECOND, 0);
              break;

          case "year":
              // Début de l'année en cours
              calendar.set(Calendar.DAY_OF_YEAR, 1);
              calendar.set(Calendar.HOUR_OF_DAY, 0);
              calendar.set(Calendar.MINUTE, 0);
              calendar.set(Calendar.SECOND, 0);
              calendar.set(Calendar.MILLISECOND, 0);
              break;

          default:
              throw new IllegalArgumentException("Période non reconnue : " + period);
      }
      return calendar.getTime();
  }
/////////////////////////////////////////comment //////////////////////////////////////////////////////

    public Map<String, Object> getCommentsStatisticsByPeriod(Date startDate, Date endDate) {
        List<Comment> comments = commentRepository.findByDateCreateBetweenAndEnabledTrue(startDate, endDate);

        // Calculer le nombre total de commentaires dans la période
        long totalComments = comments.size();

        // Initialiser les statistiques avec des valeurs de 0
        Map<Category, Long> countStatistics = Arrays.stream(Category.values())
                .collect(Collectors.toMap(Function.identity(), c -> 0L));

        // Mettre à jour les statistiques en fonction des commentaires
        for (Comment comment : comments) {
            if (comment != null && comment.getPoste() != null) {
                Poste poste = comment.getPoste();
                if (poste != null) {
                    Category category = poste.getCategory();
                    countStatistics.put(category, countStatistics.get(category) + 1);
                }
            }
        }

        // Calculer les pourcentages
        Map<Category, Double> percentageStatistics = countStatistics.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (totalComments > 0) ? (entry.getValue() * 100.0 / totalComments) : 0.0
                ));

        // Préparer le résultat final avec les pourcentages et le total des commentaires
        Map<String, Object> result = new HashMap<>();
        result.put("percentages", percentageStatistics);
        result.put("total", totalComments);

        System.out.println("Total Comments: " + totalComments);
        System.out.println("Percentage Statistics: " + percentageStatistics);

        return result;
    }

    public Map<String, Map<Category, Long>> getCommentsStatistics(String category, String period, Date selectedDate) {
        Date startDate;
        Date endDate;

        switch (period) {
            case "year":
                startDate = getStartDateForYear(selectedDate);
                endDate =DateUtils.addMonths(startDate, 11); // Date actuelle
                return getMonthlyCommentStatistics(startDate, endDate, category);

            case "month":
                startDate = getStartDateForMonth(selectedDate);
                endDate = DateUtils.addMonths(startDate, 1); // Fin du mois
                return getWeeklyCommentStatistics(startDate, endDate, category);

            case "week":
                startDate = getStartDateForWeek(selectedDate);
                endDate = DateUtils.addDays(startDate, 7); // Fin de la semaine
                return getDailyCommentStatistics(startDate, endDate, category);

            case "day":
                startDate = getStartDateForDay(selectedDate);
                endDate = selectedDate; // Jusqu'à la date actuelle
                return getHourlyCommentStatistics(startDate, endDate, category);

            default:
                throw new IllegalArgumentException("Période non reconnue: " + period);
        }
    }

    private Map<String, Map<Category, Long>> getMonthlyCommentStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> monthlyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);  // Commence à partir de startDate

        while (cal.getTime().before(endDate) || cal.getTime().equals(endDate)) {
            Date periodStart = cal.getTime();

            // Avancer d'un mois
            cal.add(Calendar.MONTH, 1);

            // Ajuster la fin de la période
            Date periodEnd = cal.getTime();

            // Si periodEnd dépasse endDate, on limite la période à endDate
            if (periodEnd.after(endDate)) {
                periodEnd = endDate;
            }

            // Formater le nom du mois et l'année pour la clé
            String key = MONTH_NAMES.get(cal.get(Calendar.MONTH) == 0 ? 11 : cal.get(Calendar.MONTH) - 1)
                    + " " + cal.get(Calendar.YEAR);

            // Ajouter les statistiques pour le mois courant
            monthlyStats.put(key, getStatisticsCommentByPeriod(periodStart, periodEnd, category, PeriodType.WEEK));
        }

        return monthlyStats;
    }


    private Map<String, Map<Category, Long>> getWeeklyCommentStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> weeklyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate); // Utilise startDate directement comme début

        while (cal.getTime().before(endDate)) {
            Date periodStart = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, 6); // Fin de la semaine à partir de startDate
            Date periodEnd = cal.getTime();

            // Limiter la fin de la semaine si nécessaire
            if (periodEnd.after(endDate)) {
                periodEnd = endDate;
            }

            String key = formatPeriod(periodStart, periodEnd);
            weeklyStats.put(key, getStatisticsCommentByPeriod(periodStart, periodEnd, category, PeriodType.DAY));

            // Passer à la semaine suivante
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return weeklyStats;
    }

    private Map<String, Map<Category, Long>> getDailyCommentStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> dailyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        System.out.println("Start Date: " + new SimpleDateFormat("yyyy-MM-dd").format(startDate));
        System.out.println("End Date: " + new SimpleDateFormat("yyyy-MM-dd").format(endDate));

        // Ajuster la startDate à 00:00:00
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0); // Minuit
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date periodStart = cal.getTime();
        System.out.println("Adjusted startDate (periodStart): " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(periodStart));

        // Ajuster la endDate à 23:59:59
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23); // 23h59
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date periodEnd = endCal.getTime();
        System.out.println("Adjusted endDate (periodEnd): " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(periodEnd));

        // Traitement des jours de la période
        while (periodStart.before(periodEnd)) {
            // Formatte la date pour la clé dans le map
            Calendar nextDayCal = Calendar.getInstance();
            nextDayCal.setTime(periodStart);
            nextDayCal.add(Calendar.DAY_OF_YEAR, 1); // Passe au jour suivant
            Date periodNext = nextDayCal.getTime();

            // Formater la date pour la clé dans le map
            String key = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRENCH).format(periodStart);
            System.out.println("Processing period: " + key + " to " + new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRENCH).format(periodNext));

            // Appeler la méthode qui récupère les statistiques pour ce jour (intervalle)
            dailyStats.put(key, getStatisticsCommentByPeriod(periodStart, periodNext, category, PeriodType.HOUR));

            // Passe au jour suivant (réajuster periodStart)
            periodStart = periodNext;}
        return dailyStats;
    }


    private Map<String, Map<Category, Long>> getHourlyCommentStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> hourlyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        // Récupère la date actuelle
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        // Si endDate est dans le futur, ajuste endDate à la date actuelle
        if (endDate.after(now.getTime())) {
            endDate = now.getTime();
        }

        // On fixe le calendrier à l'heure de début
        cal.setTime(startDate);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Vérifie si endDate est aujourd'hui
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        // Ajuste endCal si endDate est aujourd'hui
        if (endDate.equals(now.getTime())) {
            endCal.setTime(now.getTime());
        }

        // Tant que la date de début est avant la date de fin
        while (cal.getTime().before(endCal.getTime())) {
            Date periodStart = cal.getTime();  // Début de l'heure
            cal.add(Calendar.HOUR_OF_DAY, 1);  // On avance d'une heure
            Date periodEnd = cal.getTime();    // Fin de l'heure

            // Ajuste periodEnd si nécessaire
            if (periodEnd.after(endCal.getTime())) {
                periodEnd = endCal.getTime();
            }

            // Formatter pour l'affichage des heures
            String key = formatPeriod(periodStart, periodEnd);

            // Ajout des statistiques pour l'heure en cours
            hourlyStats.put(key, getStatisticsCommentByPeriod(periodStart, periodEnd, category, PeriodType.MINUTE));

            // Si periodEnd est exactement endCal, on arrête la boucle
            if (periodEnd.equals(now.getTime())) {
                break;
            }
        }

        return hourlyStats;
    }

    private Map<Category, Long> getStatisticsCommentByPeriod(Date startDate, Date endDate, String category, PeriodType periodType) {
        Map<Category, Long> countStatistics = new HashMap<>();

        if ("All".equalsIgnoreCase(category)) {
            // Récupérer tous les commentaires dans la période spécifiée
            List<Comment> comments = commentRepository.findByDateCreateBetweenAndEnabledTrue(startDate, endDate);

            // Initialiser les statistiques avec des valeurs de 0 pour chaque catégorie
            Map<Category, Long> initialStatistics = Arrays.stream(Category.values())
                    .collect(Collectors.toMap(Function.identity(), c -> 0L));

            // Mettre à jour les statistiques en fonction des commentaires
            for (Comment comment : comments) {
                if (comment != null && comment.getPoste() != null) {
                    Category commentCategory = comment.getPoste().getCategory();
                    initialStatistics.put(commentCategory, initialStatistics.get(commentCategory) + 1);
                }
            }

            countStatistics.putAll(initialStatistics);
        } else {
            // Pour un traitement avec une catégorie spécifique (non "All")
            Category categoryEnum = Category.valueOf(category);

            // Récupérer tous les commentaires dans la période spécifiée
            List<Comment> comments = commentRepository.findByDateCreateBetweenAndEnabledTrue(startDate, endDate);

            // Initialiser la statistique pour cette catégorie
            long count = 0;

            // Mettre à jour les statistiques en fonction des commentaires
            for (Comment comment : comments) {
                if (comment != null && comment.getPoste() != null) {
                    Category commentCategory = comment.getPoste().getCategory();
                    if (categoryEnum.equals(commentCategory)) {
                        count++;
                    }
                }
            }

            countStatistics.put(categoryEnum, count);
        }

        return countStatistics;
    }
/////////////////////////////////////////groupe //////////////////////////////////////////////////////

    public Map<String, Object> getGroupsStatisticsByPeriod(Date startDate, Date endDate) {
        // Récupérer les groupes créés dans la période donnée
        List<Groupe> groupes = groupeRepository.findByDateCreateBetween(startDate, endDate);

        // Calculer le nombre total de groupes dans la période
        long totalGroupes = groupes.size();

        // Initialiser les statistiques avec des valeurs de 0
        Map<Category, Long> countStatistics = Arrays.stream(Category.values())
                .collect(Collectors.toMap(Function.identity(), c -> 0L));

        // Mettre à jour les statistiques en fonction des groupes
        for (Groupe groupe : groupes) {
            Category category = groupe.getCategory();
            countStatistics.put(category, countStatistics.get(category) + 1);
        }

        // Calculer les pourcentages
        Map<Category, Double> percentageStatistics = countStatistics.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (totalGroupes > 0) ? (entry.getValue() * 100.0 / totalGroupes) : 0.0
                ));

        // Préparer le résultat final avec les pourcentages et le total des groupes
        Map<String, Object> result = new HashMap<>();
        result.put("percentages", percentageStatistics);
        result.put("total", totalGroupes);

        System.out.println("Total Groups: " + totalGroupes);
        System.out.println("Percentage Statistics: " + percentageStatistics);

        return result;
    }


    public Map<String, Map<Category, Long>> getGroupesStatistics(String category, String period, Date selectedDate) {
        Date startDate;
        Date endDate;

        switch (period) {
            case "year":
                startDate = getStartDateForYear(selectedDate);
                endDate =DateUtils.addMonths(startDate, 11); // Date actuelle
                return getMonthlyGroupeStatistics(startDate, endDate, category);

            case "month":
                startDate = getStartDateForMonth(selectedDate);
                endDate = DateUtils.addMonths(startDate, 1); // Fin du mois
                return getWeeklyGroupeStatistics(startDate, endDate, category);

            case "week":
                startDate = getStartDateForWeek(selectedDate);
                endDate = DateUtils.addDays(startDate, 7); // Fin de la semaine
                return getDailyGroupeStatistics(startDate, endDate, category);

            case "day":
                startDate = getStartDateForDay(selectedDate);
                endDate = selectedDate; // Jusqu'à la date actuelle
                return getHourlyGroupeStatistics(startDate, endDate, category);

            default:
                throw new IllegalArgumentException("Période non reconnue: " + period);
        }
    }
    private Map<String, Map<Category, Long>> getMonthlyGroupeStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> monthlyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);  // Commence à partir de startDate

        while (cal.getTime().before(endDate) || cal.getTime().equals(endDate)) {
            Date periodStart = cal.getTime();

            // Avancer d'un mois
            cal.add(Calendar.MONTH, 1);

            // Ajuster la fin de la période
            Date periodEnd = cal.getTime();

            // Si periodEnd dépasse endDate, on limite la période à endDate
            if (periodEnd.after(endDate)) {
                periodEnd = endDate;
            }

            // Formater le nom du mois et l'année pour la clé
            String key = MONTH_NAMES.get(cal.get(Calendar.MONTH) == 0 ? 11 : cal.get(Calendar.MONTH) - 1)
                    + " " + cal.get(Calendar.YEAR);

            // Ajouter les statistiques pour le mois courant
            monthlyStats.put(key, getStatisticsGroupeByPeriod(periodStart, periodEnd, category, PeriodType.WEEK));
        }

        return monthlyStats;
    }


    private Map<String, Map<Category, Long>> getWeeklyGroupeStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> weeklyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate); // Utilise startDate directement comme début

        while (cal.getTime().before(endDate)) {
            Date periodStart = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, 6); // Fin de la semaine à partir de startDate
            Date periodEnd = cal.getTime();

            // Limiter la fin de la semaine si nécessaire
            if (periodEnd.after(endDate)) {
                periodEnd = endDate;
            }

            String key = formatPeriod(periodStart, periodEnd);
            weeklyStats.put(key, getStatisticsGroupeByPeriod(periodStart, periodEnd, category, PeriodType.DAY));

            // Passer à la semaine suivante
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return weeklyStats;
    }

    private Map<String, Map<Category, Long>> getDailyGroupeStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> dailyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        System.out.println("Start Date: " + new SimpleDateFormat("yyyy-MM-dd").format(startDate));
        System.out.println("End Date: " + new SimpleDateFormat("yyyy-MM-dd").format(endDate));

        // Ajuster la startDate à 00:00:00
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0); // Minuit
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date periodStart = cal.getTime();
        System.out.println("Adjusted startDate (periodStart): " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(periodStart));

        // Ajuster la endDate à 23:59:59
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23); // 23h59
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date periodEnd = endCal.getTime();
        System.out.println("Adjusted endDate (periodEnd): " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(periodEnd));

        // Traitement des jours de la période
        while (periodStart.before(periodEnd)) {
            // Formatte la date pour la clé dans le map
            Calendar nextDayCal = Calendar.getInstance();
            nextDayCal.setTime(periodStart);
            nextDayCal.add(Calendar.DAY_OF_YEAR, 1); // Passe au jour suivant
            Date periodNext = nextDayCal.getTime();

            // Formater la date pour la clé dans le map
            String key = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRENCH).format(periodStart);
            System.out.println("Processing period: " + key + " to " + new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRENCH).format(periodNext));

            // Appeler la méthode qui récupère les statistiques pour ce jour (intervalle)
            dailyStats.put(key, getStatisticsGroupeByPeriod(periodStart, periodNext, category, PeriodType.HOUR));

            // Passe au jour suivant (réajuster periodStart)
            periodStart = periodNext;}
        return dailyStats;
    }


    private Map<String, Map<Category, Long>> getHourlyGroupeStatistics(Date startDate, Date endDate, String category) {
        Map<String, Map<Category, Long>> hourlyStats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        // Récupère la date actuelle
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        // Si endDate est dans le futur, ajuste endDate à la date actuelle
        if (endDate.after(now.getTime())) {
            endDate = now.getTime();
        }

        // On fixe le calendrier à l'heure de début
        cal.setTime(startDate);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Vérifie si endDate est aujourd'hui
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        // Ajuste endCal si endDate est aujourd'hui
        if (endDate.equals(now.getTime())) {
            endCal.setTime(now.getTime());
        }

        // Tant que la date de début est avant la date de fin
        while (cal.getTime().before(endCal.getTime())) {
            Date periodStart = cal.getTime();  // Début de l'heure
            cal.add(Calendar.HOUR_OF_DAY, 1);  // On avance d'une heure
            Date periodEnd = cal.getTime();    // Fin de l'heure

            // Ajuste periodEnd si nécessaire
            if (periodEnd.after(endCal.getTime())) {
                periodEnd = endCal.getTime();
            }

            // Formatter pour l'affichage des heures
            String key = formatPeriod(periodStart, periodEnd);

            // Ajout des statistiques pour l'heure en cours
            hourlyStats.put(key, getStatisticsGroupeByPeriod(periodStart, periodEnd, category, PeriodType.MINUTE));

            // Si periodEnd est exactement endCal, on arrête la boucle
            if (periodEnd.equals(now.getTime())) {
                break;
            }
        }

        return hourlyStats;
    }


    private Map<Category, Long> getStatisticsGroupeByPeriod(Date startDate, Date endDate, String category, PeriodType periodType) {
        Map<Category, Long> statistics = new HashMap<>();

        if ("All".equalsIgnoreCase(category)) {
            // Récupérer les postes pour toutes les catégories entre les dates spécifiées
            List<Groupe> groupes= groupeRepository.findByDateCreateBetween(startDate, endDate);

            // Calculer le nombre total de postes dans la période


            // Initialiser les statistiques avec des valeurs de 0 pour chaque catégorie
            Map<Category, Long> countStatistics = Arrays.stream(Category.values())
                    .collect(Collectors.toMap(Function.identity(), c -> 0L));

            // Mettre à jour les statistiques en fonction des postes
            for (Groupe groupe : groupes) {
                Category postCategory = groupe.getCategory();
                countStatistics.put(postCategory, countStatistics.get(postCategory) + 1);
            }

            statistics.putAll(countStatistics);
        } else {
            // Si une catégorie spécifique est fournie
            Category categoryEnum = Category.valueOf(category);

            // Récupérer les postes de la catégorie donnée entre les dates spécifiées
            List<Groupe> groupes= groupeRepository.findByCategoryAndDateCreateBetween(categoryEnum, startDate, endDate);

            // Initialiser la statistique pour cette catégorie
            long count = groupes.size();
            statistics.put(categoryEnum, count);
        }

        return statistics;
    }

    //////////////////////////////////////////user///////////////////////////////////////

    public List<Map<String, Object>> getUserStatisticsByGranularity(Date startDate, Date endDate, String granularity) {

        List<User> users = userRepository. findByDateAuthenticatedBetweenAndEnabledTrueAndRole(startDate, endDate, Role.USER);
        List<Map<String, Object>> statistics = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Déterminer le type de granularité
        switch (granularity.toLowerCase()) {
            case "year":
                while (calendar.getTime().before(endDate)) {
                    Map<String, Object> dataPoint = new HashMap<>();
                    int year = calendar.get(Calendar.YEAR);
                    long count = users.stream()
                            .filter(user -> isSameYear(user.getDateAuthenticated(), year))
                            .count();
                    double percentage = (users.size() > 0) ? (count * 100.0 / users.size()) : 0.0;
                    dataPoint.put("period", year);
                    dataPoint.put("percentage", percentage);
                    dataPoint.put("total", count);  // Ajout du nombre total

                    statistics.add(dataPoint);
                    calendar.add(Calendar.YEAR, 1);
                    calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                }
                break;

            case "month":
                while (calendar.getTime().before(endDate)) {
                    Map<String, Object> dataPoint = new HashMap<>();
                    String monthYear = String.format("%d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
                    long count = users.stream()
                            .filter(user -> isSameMonth(user.getDateAuthenticated(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)))
                            .count();
                    double percentage = (users.size() > 0) ? (count * 100.0 / users.size()) : 0.0;
                    dataPoint.put("period", monthYear);
                    dataPoint.put("percentage", percentage);
                    dataPoint.put("total", count);  // Ajout du nombre total

                    statistics.add(dataPoint);
                    calendar.add(Calendar.MONTH, 1);
                }
                break;

            case "week":
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH); // Format pour les dates
                while (calendar.getTime().before(endDate)) {
                    Map<String, Object> dataPoint = new HashMap<>();

                    // Obtenir la première date (lundi) de la semaine
                    Calendar startOfWeek = (Calendar) calendar.clone();
                    startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                    // Obtenir la dernière date (dimanche) de la semaine
                    Calendar endOfWeek = (Calendar) startOfWeek.clone();
                    endOfWeek.add(Calendar.DAY_OF_WEEK, 6);

                    // Créer une plage de dates lisible
                    String weekRange = String.format("%s - %s",
                            dateFormat.format(startOfWeek.getTime()),
                            dateFormat.format(endOfWeek.getTime()));

                    // Compter les utilisateurs pour la semaine
                    long count = users.stream()
                            .filter(user -> isSameWeek(user.getDateAuthenticated(), calendar.get(Calendar.YEAR), calendar.get(Calendar.WEEK_OF_YEAR)))
                            .count();

                    // Calculer le pourcentage
                    double percentage = (users.size() > 0) ? (count * 100.0 / users.size()) : 0.0;

                    // Ajouter les données
                    dataPoint.put("period", weekRange); // Utiliser la plage de dates
                    dataPoint.put("percentage", percentage);
                    dataPoint.put("total", count);  // Ajout du nombre total

                    statistics.add(dataPoint);

                    // Passer à la semaine suivante
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                }
                break;


            case "day":
                while (calendar.getTime().before(endDate)) {
                    Map<String, Object> dataPoint = new HashMap<>();
                    String day = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                    long count = users.stream()
                            .filter(user -> isSameDay(user.getDateAuthenticated(), calendar.getTime()))
                            .count();
                    double percentage = (users.size() > 0) ? (count * 100.0 / users.size()) : 0.0;
                    dataPoint.put("period", day);
                    dataPoint.put("percentage", percentage);
                    dataPoint.put("total", count);  // Ajout du nombre total

                    statistics.add(dataPoint);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                break;

            case "hour":
                while (calendar.getTime().before(endDate)) {
                    Map<String, Object> dataPoint = new HashMap<>();
                    String hour = String.format("%d-%02d-%02d %02d:00", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY));
                    long count = users.stream()
                            .filter(user -> isSameHour(user.getDateAuthenticated(), calendar.getTime()))
                            .count();
                    double percentage = (users.size() > 0) ? (count * 100.0 / users.size()) : 0.0;
                    dataPoint.put("period", hour);
                    dataPoint.put("percentage", percentage);
                    dataPoint.put("total", count);  // Ajout du nombre total

                    statistics.add(dataPoint);
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
                break;

            default:
                throw new IllegalArgumentException("Granularity type not supported.");
        }

        return statistics;}
    private boolean isSameYear(Date date, int year) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR) == year;
    }

    private boolean isSameMonth(Date date, int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month;
    }

    private boolean isSameWeek(Date date, int year, int week) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR) == year && cal.get(Calendar.WEEK_OF_YEAR) == week;
    }

    private boolean isSameDay(Date date, Date targetDate) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        cal2.setTime(targetDate);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameHour(Date date, Date targetDate) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        cal2.setTime(targetDate);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY);
    }
    public Map<String, Double> calculateUserStatusPercentage() {
        long totalUsers = userRepository.count();
        Map<String, Double> percentages = new HashMap<>();

        if (totalUsers == 0) {
            percentages.put("connectedPercentage", 0.0);
            percentages.put("disconnectedPercentage", 0.0);
            percentages.put("connectedCount", 0.0);
            percentages.put("disconnectedCount", 0.0);
            return percentages;
        }

        long connectedUsers = userRepository.countByStatus(Status.CONNECTE);
        long disconnectedUsers = userRepository.countByStatus(Status.DECONNECTE);

        double connectedPercentage = (double) connectedUsers / totalUsers * 100;
        double disconnectedPercentage = (double) disconnectedUsers / totalUsers * 100;

        // Ajouter les pourcentages
        percentages.put("connectedPercentage", connectedPercentage);
        percentages.put("disconnectedPercentage", disconnectedPercentage);

        // Ajouter les nombres d'utilisateurs
        percentages.put("connectedCount", (double) connectedUsers);
        percentages.put("disconnectedCount", (double) disconnectedUsers);

        return percentages;
    }
}
