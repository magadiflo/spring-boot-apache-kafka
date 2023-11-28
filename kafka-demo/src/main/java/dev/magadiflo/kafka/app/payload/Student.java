package dev.magadiflo.kafka.app.payload;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Student {
    private int id;
    private String firstname;
    private String lastname;
}
