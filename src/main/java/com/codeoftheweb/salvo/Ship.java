package com.codeoftheweb.salvo;


import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String shipType;

    @ElementCollection
    private List<String> shipLocations;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="Ship_id")
    private GamePlayer gamePlayer;

    public Ship() {

    }
    public long getId(){
        return this.id;
    }




    public Ship(String first, GamePlayer second, List third) {
        this.shipType = first;
        this.gamePlayer = second;
        this.shipLocations = third;
    }
    public String getShipType(){
        return this.shipType;
    }

    public void setShipType(String string){
        this.shipType = string;
    }
//    public GamePlayer getGamePlayer(){
//        return this.gamePlayer;
//    }
    public void setGamePlayer(GamePlayer gamePlayer){
        this.gamePlayer = gamePlayer;
    }
    public List getShipLocations(){
        return this.shipLocations;
    }





}