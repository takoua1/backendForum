package com.web.forumSocialX.message;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.web.forumSocialX.groupe.Groupe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageWrapper {
    @JsonProperty("message")
    private Message message;
    @JsonProperty("groupe")
private Groupe groupe;
    @JsonProperty("fileUrl")
    private String fileUrl;
    @JsonProperty("fileType")
    private String fileType;

}
