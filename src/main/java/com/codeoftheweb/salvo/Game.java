package com.codeoftheweb.salvo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import static java.util.stream.Collectors.toList;
@Entity
public class Game {



    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public long id;
    private String gameName;
    private Date creationDate;

    public Game() { }

    public Game(String first, Date last) {
        this.gameName = first;
        this.creationDate = last;

    }

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<Score> scores;

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        gamePlayers.add(gamePlayer);
    }

    public long getId(){
        return id;
    }
    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    public Date getDate(){
        return creationDate;
    }
    public void setDate(Date date){
        this.creationDate = date;
    }
    public String toString() {
        return gameName ;
    }
    public List<Player> getPlayers() {
        return gamePlayers.stream().map(gamePlayer -> gamePlayer.getPlayer()).collect(toList());

    }


    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }
//    public Set<Score> getScores() {
//        return scores;
//    }


    public List<Salvo>  getSalvoesList(){
        List list = new ArrayList();
         gamePlayers.stream().forEach(gamePlayer -> list.addAll(gamePlayer.getSalvoes()));
         return list;
    }
    public Boolean areTwoPlayersInGame(Game game){
       long amountOfPlayers =  game.getGamePlayers().stream().count();
       Boolean twoPlayers = null;
       if(amountOfPlayers == 2){
           twoPlayers = true;
       }else{
           twoPlayers = false;
       }
       return  twoPlayers;
    }

}

