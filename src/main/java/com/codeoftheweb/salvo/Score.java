package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="Game")
    private Game game;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player")
    private Player player;


    private Double score;
    private Date finishDate;

    public Score() { }

    public Score(Game game, Player player, Double third, Date fourth) {

        this.game = game;
        this.player = player;
        this.score = third;
        this.finishDate = fourth;
    }
    public long getScoreId(){
        return this.id;
    }

    public Double getScore(){
        return this.score;
    }
    public Game getGame(){
        return this.game;
    }
    public Player getPlayer(){    //change name to getPlayerx --> player will be seen in http://localhost:8080/rest/players/1/scores
        return this.player;
    }

    public void setScore(Double score){
        this.score = score;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }
}

