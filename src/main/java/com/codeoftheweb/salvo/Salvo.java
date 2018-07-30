package com.codeoftheweb.salvo;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ElementCollection
    private List<String> salvoLocations;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="Salvo_id")
    private GamePlayer gamePlayer;

    private long turnNumber;
    public Salvo() {

    }


    public Salvo(GamePlayer first, long second, List third) {
        this.gamePlayer = first;
        this.turnNumber = second;
        this.salvoLocations = third;
    }
    public long getSalvoId(){
        return this.id;
    }
    public long getTurn(){
        return this.turnNumber;
    }
    public GamePlayer getGamePlayer(){
        return this.gamePlayer;
    }

    public List getSalvoLocations(){
        return this.salvoLocations;
    }
//    public Salvo getSalvo(){
//        return Salvo.this;
//    }
    public void setGamePlayer(GamePlayer gamePlayer){
        this.gamePlayer = gamePlayer;
    }
    public void setTurnNumber(Long turnNumber){
        this.turnNumber = turnNumber;
    }
    public Long getTurnNumber(){
        return this.turnNumber;
    }





}