package com.codeoftheweb.salvo;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    private Date date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="Game_id")
    private Game game;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    Set<Ship> ships;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    Set<Salvo> salvoes;




    public GamePlayer () {

    }
    public GamePlayer(Game game, Player gamePlayer, Date date) {
        this.game = game;
        this.player = gamePlayer;
        this.date = date;
    }
    public long getId(){
        return this.id;
    }
    public Date getDate(){
        return this.date;
    }

    public Game getGame(){
        return game;
    }
    public void setGame(Game game){
        this.game = game;
    }
    public Player getPlayer(){
        return player;
    }

    public List<Player> getGamePlayers(){
        List<Player> gamePlayers = new ArrayList<>();
        gamePlayers.add(getPlayer());
        return gamePlayers;
    }


    public void setGamePlayer(Player gamePlayer){
        this.player = gamePlayer;
    }

    public void setDate(Date date){
        this.date = date;
    }
    public Set<Ship> getShips() {
        return ships;
    }
    public Set<Salvo> getSalvoes() {
        return salvoes;
    }
    public Score getScore(Game game){
      return player.getScores().stream().filter(score -> score.getGame().equals(game)).findFirst().orElse(null);
    }
    public List<Long> getListOfTurns(){
        List turnList = new ArrayList();
        getSalvoes().forEach(salvo -> turnList.add(salvo.getTurnNumber()));
        return  turnList;
    }
    public GamePlayer getOtherGamePlayer(GamePlayer gamePlayer){
        GamePlayer otherGp =null;
        if(gamePlayer.getGame().getGamePlayers().size() == 2){
            otherGp = getGame().getGamePlayers().stream().filter(gamePlayer1 -> gamePlayer1 != gamePlayer).findFirst().get();
        }else{
            otherGp = null;
        }

         return otherGp;
    }
    public Salvo getSalvoByTurn(Long turn){
// switched get() to orElse()
        Salvo salvo = null;
        if(turn!=null){
           salvo =  getSalvoes().stream().filter(salvo1 -> salvo1.getTurn() == turn).findFirst().orElse(null);
        }else{
            salvo = null;
        }

       return salvo;
    }
    public List<String> getAllShipLocations(GamePlayer gamePlayer){
        List listOfShipLocations = new ArrayList();
     gamePlayer.getShips().stream().forEach(ship -> ship.getShipLocations().stream().forEach(string->listOfShipLocations.add(string)));
    return listOfShipLocations;
    }
    public Boolean bothGPpostedSalvo(GamePlayer gamePlayer){
        if(gamePlayer.getSalvoes().size() !=0 && gamePlayer.getOtherGamePlayer(gamePlayer).getSalvoes().size() != 0){
            return true;
        }else{
            return false;
        }
    }
    public List<String> getOtherGPSalvoLoc(GamePlayer gamePlayer, Long turn){
        List listOfOtherGPSalvoLoc = new ArrayList();
        //added condition for sake of nullpointer
        if(bothGPpostedSalvo(gamePlayer)){
            getOtherGamePlayer(gamePlayer).getSalvoByTurn(turn).getSalvoLocations().stream().forEach(string-> listOfOtherGPSalvoLoc.add(string));
        }

        return listOfOtherGPSalvoLoc;
    }
    public List<Ship> shotShips(GamePlayer gamePlayer, Long turn){
//        Predicate<Ship> checkShip = string -> getOtherGPSalvoLoc(gamePlayer, turn).contains(string);
        List<Ship> listOfShipsHit = new ArrayList();
        getShips().forEach(ship -> ship.getShipLocations().stream().forEach(
                string -> {
                    if (getOtherGPSalvoLoc(gamePlayer, turn).contains(string)) {
                        if(!(listOfShipsHit.contains(ship))){
                            listOfShipsHit.add(ship);
                        }

                    }
                }
        ));
        return listOfShipsHit;
    }
    public Ship getShipById(long Id){
        return getShips().stream().filter(ship -> ship.getId() == Id).findFirst().get();
    }

    public Integer howManyTimesWasHit(Ship ship, GamePlayer gamePlayer, Long turn){
//        Integer immutable xD fuck
        AtomicReference<Integer> timesHit = new AtomicReference<>(0);

        ship.getShipLocations().stream().forEach(
                string -> {
                    if (getOtherGPSalvoLoc(gamePlayer, turn).contains(string)) {

                           timesHit.updateAndGet(v -> v + 1);


                    }
                }
        );

        return timesHit.get();
    }

    public Boolean isShipSunk(Ship ship, GamePlayer gamePlayer, Long turn){

       AtomicReference<Boolean> shipSunk = new AtomicReference<>(false);
       List turns = gamePlayer.getListOfTurns();
       Integer lenght = ship.getShipLocations().size();
        AtomicReference<Integer> timesHit = new AtomicReference<>(0);
        ship.getShipLocations().stream().forEach(
                string -> {
                    if (getOtherGPSalvoTillTurn(gamePlayer, turn).contains(string)) {

                        timesHit.updateAndGet(v -> v + 1);
                    }
                    if(timesHit.get() == lenght){
                        shipSunk.set(true);
                    }
                }
        );

        return shipSunk.get();
    }
    public List<String> getOtherGPSalvoTillTurn(GamePlayer gamePlayer, Long turn){
        final List<String> listOfSalvoesLocTillTurn = new ArrayList();
        //entering if condition so it would run when
        if(gamePlayer.getOtherGamePlayer(gamePlayer).getSalvoes().size() >= 1){

            getListOfTurns().stream().filter(turn1 -> turn1 <=turn).forEach(turn1 ->
                    getOtherGamePlayer(gamePlayer).getSalvoByTurn(turn1).getSalvoLocations().stream().forEach(string-> listOfSalvoesLocTillTurn.add((String) string)));
        }else{
            //listOfSalvoesLocTillTurn = null;
        }

        return listOfSalvoesLocTillTurn;
    }
    public AtomicReference<Integer> getShipsLeftOnTurn(GamePlayer gamePlayer, Long turn){

        AtomicReference<Integer> amountOfShipsStart = new AtomicReference<>(gamePlayer.getShips().size());
//        Integer lenght = gamePlayer.getShips().stream().map(ship -> ship.getShipLocations().size());
        AtomicReference<Integer> timesHit = new AtomicReference<>(0);
        gamePlayer.getShips().forEach( ship -> {
            if (getOtherGPSalvoTillTurn(gamePlayer, turn).containsAll(ship.getShipLocations())) {

                amountOfShipsStart.updateAndGet(v -> v - 1);
            }

        });
        return amountOfShipsStart;
    }
    public Boolean are2playersInGame(GamePlayer gamePlayer){
        AtomicReference<Integer> amountOfPlayers = new AtomicReference<>(0);
        gamePlayer.getGame().getGamePlayers().forEach(gamePlayer1 -> {
            amountOfPlayers.updateAndGet(v -> v + 1);
        });
        Boolean twoPlayers;
        if(amountOfPlayers.get() == 2){
            twoPlayers= true;
        }
        else{
             twoPlayers= false;
        }
        return twoPlayers;
    }
    public Boolean areShipsPlaced(GamePlayer gamePlayer){
        Boolean shipsPlaced = null;
        AtomicReference<Integer> numberOfShips = new AtomicReference<>(0);
        if(gamePlayer.getGame().getGamePlayers().size() == 2){
            gamePlayer.getShips().forEach(ship -> {
                numberOfShips.updateAndGet(v -> v + 1);
            });
        }
        if(numberOfShips.get() == 5){
            shipsPlaced = true;
        }else{
            shipsPlaced = false;
        }
        return shipsPlaced;
    }
    public Long getFirstGP(Game game){
        List<Long> listOfIds = new ArrayList<>();
        Long gpID = null;
        game.getGamePlayers().stream().map(gamePlayer -> gamePlayer.getId()).forEach(id -> listOfIds.add(id));
        if(game.getGamePlayers().size() == 2){
            if(listOfIds.get(0) < listOfIds.get(1)){
                gpID = listOfIds.get(0);
            }
            if(listOfIds.get(1) < listOfIds.get(0)){
                gpID = listOfIds.get(1);
        }


//        GamePlayer firstGP = gamePlayerRepository.findGamePlayerById(gpID);
        }
        return gpID;
    }



  }








