package com.web.forumTunisia.statistics;


import com.web.forumTunisia.poste.Poste;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class StatisticsResponse {
    private List<Poste> categories;
}
