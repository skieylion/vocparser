package jentus.parser.wordparser.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name="ContextA")
@Data
public class ContextA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String text;

    @Column
    private String word;

    @Column
    private String kind;

    @Column
    private String parts;

    @Column
    private String def;

}