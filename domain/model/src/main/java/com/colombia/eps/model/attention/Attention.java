package com.colombia.eps.model.attention;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Attention {
    private String id;
    private String patientEmail;
    private String doctorName;
    private String reasonConsult;
    private String diagnostic;

    @Override
    public String toString() {
        return "Attention{" +
                "id='" + id + '\'' +
                ", patientEmail='" + patientEmail + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", reasonConsult='" + reasonConsult + '\'' +
                ", diagnostic='" + diagnostic + '\'' +
                '}';
    }
}
