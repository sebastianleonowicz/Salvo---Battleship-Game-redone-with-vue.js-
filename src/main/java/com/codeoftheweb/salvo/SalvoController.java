package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.Game;
import com.codeoftheweb.salvo.GameRepository;
import com.codeoftheweb.salvo.GamePlayer;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;
    @Autowired
    private ScoreRepository scoreRepository;

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
    private Player loggedPlayer(Authentication authentication){
        return playerRepository.findByEmail(authentication.getName());
    }

    @RequestMapping("/games")
    public Map<String, Object> getAllGames(Authentication authentication) {

        List list = new ArrayList();
        Map<String, Object> dto = new LinkedHashMap<>();
        Map<String, Object> dto2 = new LinkedHashMap<>();
        if(authentication == null){
            dto.put("user", null);
            list=gameRepository
                    .findAll()
                    .stream()
                    .map(game -> makeGameDTO(game))
                    .collect(toList());

            dto.put("games", list );
        }else{
            dto2.put("id", playerRepository.findByEmail(authentication.getName()).getId());
            dto2.put("name", playerRepository.findByEmail(authentication.getName()).getEmail());
            dto.put("user", dto2);

            list=gameRepository
                    .findAll()
                    .stream()
                    .map(game -> makeGameDTO(game))
                    .collect(toList());

            dto.put("games", list );
        }

        return dto;
    }
    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getDate());
//        dto.put("pet", makePetDTO(owner.getPet()));
        dto.put("gamePlayer", game.getGamePlayers().stream()
                                                    .map(gp -> makeGamePlayerDTO(gp))
                                                    .collect(Collectors.toList()));
        return dto;
    }
    private  Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        dto.put("score", gamePlayer.getScore(gamePlayer.getGame()));

        return dto;

    }
    private Map<String, Object> makePlayerDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("name", player.getLastName());

        return dto;
    }
    @RequestMapping("/leaderboard")
    private List<Object> getAllPlayers(){
        return playerRepository.findAll().stream().map(player -> makeLeaderboardDTO(player)).collect(toList());
    }
    private Map<String, Object> makeLeaderboardDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<>();
        Double totalScore = 0.0;
        Integer wins = 0;
        Integer losses = 0;
        Integer ties = 0;
        for(Score score : player.getScores()){
          if(score.getScore() == 1.0){
              wins += 1;
              totalScore += 1;
          }
          if(score.getScore() == 0.5){
                ties +=1; }
                totalScore += 0.5;
          if(score.getScore() == 0.0){
              losses +=1;
          }

        }
        dto.put("name", (player.getFirstName()+" "+player.getLastName()));
        dto.put("totalScore", totalScore);
        dto.put("numberOfWins", wins);
        dto.put("numberOfLosses", losses);
        dto.put("numberOfTies", ties);
        return dto;
    }
    @RequestMapping("/players")
    public ResponseEntity<Map<String, Object>> createGame(@RequestParam String name, String password) {
        if (name.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No name"), HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByEmail(name);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Name in use"), HttpStatus.CONFLICT);
        }
        Player newPlayer = playerRepository.save(new Player(name, name, password, name)); //2nd positiion ""
        return new ResponseEntity<>(makeMap("name", newPlayer.getFirstName()) , HttpStatus.CREATED);
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createUser(Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "No user logged in"), HttpStatus.FORBIDDEN);
        }else{
            Player player = loggedPlayer(authentication);
            java.util.Date date=new java.util.Date();
            Game game = new Game("game", date);
            gameRepository.save(game);
            GamePlayer gp = new GamePlayer(game, player, date);
            gamePlayerRepository.save(gp);
            return new ResponseEntity<>(makeMap("gpId", gp.getId()), HttpStatus.CREATED);
        }

    }


    @RequestMapping("/game/{gameId}/players")
    private ResponseEntity<Map<String, Object>> joinGame(@PathVariable long gameId, Authentication authentication){
        Game game = gameRepository.findOne(gameId);
        if(authentication == null){
            return new ResponseEntity<>(makeMap("error", "no user logged in"), HttpStatus.UNAUTHORIZED);
        }
        if(gameRepository.findById(gameId)== null){
            return new ResponseEntity<>(makeMap("error", "no such game"), HttpStatus.FORBIDDEN);
        }
        if(gameRepository.findById(gameId).getPlayers().size() == 2){
            return new ResponseEntity<>(makeMap("error", "game is full"), HttpStatus.FORBIDDEN);
        }else{
            java.util.Date date=new java.util.Date();
            GamePlayer gamePlayer = new GamePlayer(game, loggedPlayer(authentication), date);
            gamePlayerRepository.save(gamePlayer);
            return new ResponseEntity<>(makeMap("done", "game joined"), HttpStatus.CREATED);
        }
    }
    @RequestMapping(value="/games/players/{gamePlayerId}/ships", method=RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> postShips(@PathVariable long gamePlayerId, @RequestBody List<Ship> ships, Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.findGamePlayerById(gamePlayerId);

        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "No user logged in"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer == null){
            return new ResponseEntity<>(makeMap("error", "No game player with given id"), HttpStatus.UNAUTHORIZED);
        }
        if(!(loggedPlayer(authentication)== gamePlayer.getPlayer())){
            return new ResponseEntity<>(makeMap("error", "Currently logged user is not the gamePlayer ID references "), HttpStatus.UNAUTHORIZED);
        }
        if ((gamePlayer.getShips().size() == 5)){
            return new ResponseEntity<>(makeMap("error", "Ships already placed"), HttpStatus.FORBIDDEN);
        }
        else{
            ships.forEach(ship -> {
                ship.setGamePlayer(gamePlayer);
                shipRepository.save(ship);
            });

            return new ResponseEntity<>(makeMap("done", "Ships saved in ShipRepository"), HttpStatus.CREATED);
        }




    }
    @RequestMapping(value="games/players/{gamePlayerId}/salvoes", method=RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> postSalvoes(@PathVariable long gamePlayerId, @RequestBody Salvo salvo, Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.findGamePlayerById(gamePlayerId);
        Salvo postedSalvo = salvo;
        Predicate<Salvo> checkSalvo= sal -> (sal.getTurn()== postedSalvo.getTurn());

        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "No user logged in"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer == null){
            return new ResponseEntity<>(makeMap("error", "No game player with given id"), HttpStatus.UNAUTHORIZED);
        }
        if(!(loggedPlayer(authentication)== gamePlayer.getPlayer())){
            return new ResponseEntity<>(makeMap("error", "Currently logged user is not the gamePlayer ID references "), HttpStatus.UNAUTHORIZED);
        }

        if(gamePlayer.getSalvoes().stream().anyMatch(checkSalvo)){
            return new ResponseEntity<>(makeMap("error", "Salvo already shot for this turn"), HttpStatus.FORBIDDEN);
        }else{

                salvo.setGamePlayer(gamePlayer);
//                salvo.setTurnNumber(postedSalvo.getTurn());
                salvoRepository.save(salvo);

            return new ResponseEntity<>(makeMap("done", "Salvoes saved in SalvoRepository"), HttpStatus.CREATED);
        }


    }

    @RequestMapping("/game_view/{gamePlayerId}")
    private ResponseEntity<Map<String, Object>> authorize(@PathVariable long gamePlayerId, Authentication authentication){
        GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);
        Predicate<GamePlayer> checkGP = gp -> (gp.getId()== gamePlayerId);
        if (!(authentication == null) && loggedPlayer(authentication).getGamePlayers().stream().anyMatch(checkGP)){
//            used to return gamePlayerView
            Map<String, Object> gamePlayerView = new LinkedHashMap<>();
            gamePlayerView.put("id", gamePlayer.getId());
            gamePlayerView.put("created", gamePlayer.getDate());
            gamePlayerView.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream()
                    .map(pl -> makeGamePlayerDTO(pl))
                    .collect(toList()));
            gamePlayerView.put("ships", gamePlayer.getShips().stream()
                    .map(sh -> makeShipsDTO(sh))
                    .collect(toList()));
            gamePlayerView.put("salvoes", gamePlayer.getGame().getSalvoesList().stream()
                    .map(salvo -> makeSalvoDTO(salvo))
                    .collect(toList()));
            gamePlayerView.put("hitsAndSinks", gamePlayer.getListOfTurns().stream()
                    .map(turn -> makeTurnDTO(turn, gamePlayer))
                    .collect(toList()));
            gamePlayerView.put("gameStatus", gamePlayer.getGame().getGamePlayers().stream().map(this::getGameStatus).collect(toList()));

            return new ResponseEntity<>(gamePlayerView, HttpStatus.OK);
        }else{
           return new ResponseEntity<>(makeMap("error", "not authorized"), HttpStatus.UNAUTHORIZED);
        }
    }
    //Creates statuses --those will be used later to implement logic for playing the game
    private Map<String, Object> getGameStatus(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("gpId", gamePlayer.getId());
        dto.put("status", getStatusDTO(gamePlayer));


        return dto;
    }
    public Map<String, Object> getStatusDTO(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<>();
//        dto.put("firstGP", isFirstGPInGame(gamePlayer)); //checks if this GP starts first // absolete, GPs will shoot at the same time
            dto.put("canPlaceShips", canPlaceShips(gamePlayer)); // checks if this GP can place ships
            dto.put("allShipsPlaced", gamePlayer.areShipsPlaced(gamePlayer)); // checks if both GPs placed ships
            dto.put("currentTurnNumber", getCurrentTurn(gamePlayer)); // checks and updates current turn
        dto.put("canFireSalvoes", canFireSalvoes(gamePlayer)); // checks if GP can fire salvo

        if(gamePlayer.areShipsPlaced(gamePlayer)){
            dto.put("isItGameOver", isItGameOver(gamePlayer)); // checks if it's GameOver
            dto.put("didIwin", didIWin(gamePlayer));//checks if this gp won if GameOver == true;
            dto.put("isItDraw",checkIfItsDraw(gamePlayer));
            if(gameScored(gamePlayer)){
                dto.put("exactScore", gamePlayer.getScore(gamePlayer.getGame()).getScore());
            }

        }

//        }
//
//
        return dto;
    }
    public Boolean gameScored(GamePlayer gamePlayer){
        if(gamePlayer.getScore(gamePlayer.getGame()) != null){
            return true;
        }else{
            return false;
        }
    }

    public Boolean canFireSalvoes(GamePlayer gamePlayer){
        Boolean canFire = false;
       Long turn =  getCurrentTurn(gamePlayer);

        if( gamePlayer.getSalvoByTurn(turn) == null && gamePlayer.areShipsPlaced(gamePlayer) && getCurrentTurn(gamePlayer) == getCurrentTurn(gamePlayer.getOtherGamePlayer(gamePlayer)) && getCurrentTurn(gamePlayer) != null){

            canFire = true;
        }

        return canFire;
    }
    public Boolean checkIfItsDraw(GamePlayer gamePlayer){
        Long turn = getCurrentTurn(gamePlayer);
        if(gamePlayer.getShipsLeftOnTurn(gamePlayer, turn).get() == 0 && gamePlayer.getShipsLeftOnTurn(gamePlayer.getOtherGamePlayer(gamePlayer), turn).get() == 0){
            Double tied =0.5;
            java.util.Date date=new java.util.Date();
            Score score1 = new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), tied, date );
            if(gameScored(gamePlayer) == false){
                scoreRepository.save(score1);
            }

            return true;
        }else{
            return false;
        }
    }


    public Boolean didIWin(GamePlayer gamePlayer){
    Long turn = getCurrentTurn(gamePlayer);
        Double win = 1.0;
        Double lost =0.0;
        java.util.Date date=new java.util.Date();
    if( haveBothGPsShotThisTurn(gamePlayer) && (gamePlayer.getShipsLeftOnTurn(gamePlayer.getOtherGamePlayer(gamePlayer), turn).get() == 0 && gamePlayer.getShipsLeftOnTurn(gamePlayer, turn).get() != 0) && gamePlayer.areShipsPlaced(gamePlayer) == true){

        Score score1 = new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), lost, date );
        if(gameScored(gamePlayer) == false){
            scoreRepository.save(score1);
        }
        return true;
    }else{
        Score score2 = new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), win, date );
        if(gameScored(gamePlayer) == false){
            scoreRepository.save(score2);
        }
        return false;
    }
}
// checks if  its game over -- getShips() == 0;
    public Boolean isItGameOver(GamePlayer gamePlayer){
        Long turn = getCurrentTurn(gamePlayer);
        if((haveBothGPsShotThisTurn(gamePlayer) && gamePlayer.getShipsLeftOnTurn(gamePlayer, turn).get() == 0) && gamePlayer.areShipsPlaced(gamePlayer) == true
                || haveBothGPsShotThisTurn(gamePlayer.getOtherGamePlayer(gamePlayer)) && gamePlayer.getShipsLeftOnTurn(gamePlayer.getOtherGamePlayer(gamePlayer), turn).get() == 0 && gamePlayer.getOtherGamePlayer(gamePlayer).areShipsPlaced(gamePlayer.getOtherGamePlayer(gamePlayer)) ){
           return true;
        }else{
            return false;
        }
    }
    public Boolean haveBothGPsShotThisTurn(GamePlayer gamePlayer){
        if(gamePlayer.getSalvoes().size() == gamePlayer.getOtherGamePlayer(gamePlayer).getSalvoes().size()){
            return true;
        }else{
            return false;
        }
    }

    public Boolean isFirstGPInGame(GamePlayer gamePlayer){
        if(gamePlayer.getGame().getGamePlayers().size()==2){
            if(gamePlayerRepository.getOne(gamePlayer.getFirstGP(gamePlayer.getGame())).getId() == gamePlayer.getId()){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }

    }
    public Long getCurrentTurn(GamePlayer gamePlayer){
        Long turn = null;
        //checking if both GPs placed ships and if there are 2 GPs in game
        if(gamePlayer.areShipsPlaced(gamePlayer) == true && gamePlayer.getOtherGamePlayer(gamePlayer).areShipsPlaced(gamePlayer.getOtherGamePlayer(gamePlayer)) == true && gamePlayer.getGame().getGamePlayers().size() == 2){
            //if there are no salvoes, start of the game
            if(gamePlayer.getSalvoes().size() == 0){
                turn = Long.valueOf(1);
            }
            //if both players posted same amount of salvoes
            if(gamePlayer.getSalvoes().size() == gamePlayer.getOtherGamePlayer(gamePlayer).getSalvoes().size() ){
                turn = Long.valueOf(1+ gamePlayer.getSalvoes().size());
            }
            //if this gp has more salvoes than the other one
            if(gamePlayer.getSalvoes().size() > gamePlayer.getOtherGamePlayer(gamePlayer).getSalvoes().size()){
                turn = Long.valueOf(gamePlayer.getSalvoes().size());
            }
            //if this gp has less salvoes than the other one
            //!important
            if(gamePlayer.getSalvoes().size() < gamePlayer.getOtherGamePlayer(gamePlayer).getSalvoes().size()){
               turn = Long.valueOf(gamePlayer.getOtherGamePlayer(gamePlayer).getSalvoes().size());
            }

        }
        return turn;
    }

//    public Boolean canFireSalvoes(GamePlayer gamePlayer){
//        if(isFirstGPInGame(gamePlayer) == true && gamePlayer.){
//
//        }
//    }
    public Boolean canPlaceShips(GamePlayer gamePlayer){
      if(gamePlayer.getShips().size() == 5){
          return false;
      }else {
          return true;
      }
    }

    //Creates TurnDTO - needed to properly display planned json of hits ans sinks
    private Map<String, Object> makeTurnDTO(Long turn, GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<>();

            dto.put("turnNumber", turn);
            dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream()
                    .map(gamePlayer1 -> makeGamePlayerHitsDTO(gamePlayer1, turn))
                    .collect(toList()));




        return dto;
    }
    private Map<String, Object> makeGamePlayerHitsDTO(GamePlayer gamePlayer, Long turn){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("GPid", gamePlayer.getId());
        if(gamePlayer.getSalvoes().size() == gamePlayer.getOtherGamePlayer(gamePlayer).getSalvoes().size()){
            dto.put("hitsOnThisGP", gamePlayer.shotShips(gamePlayer, turn).stream().map(ship -> createHitsObject(ship, turn, gamePlayer)).collect(toList()));
            dto.put("shipsLeft", gamePlayer.getShipsLeftOnTurn(gamePlayer, turn));
        }
        return dto;
    }
    private Map<String, Object> createHitsObject(Ship ship, Long turn, GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("shipId", ship.getId());
        dto.put("shipName", ship.getShipType());
        dto.put("hitTimes", gamePlayer.howManyTimesWasHit(ship, gamePlayer, turn));
        dto.put("shipSunk", gamePlayer.isShipSunk(ship, gamePlayer, turn));



        return  dto;

    }


    private Map<String, Object> makeSalvoDTO(Salvo salvo){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }



    private Map<String, Object> makeShipsDTO(Ship ship){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getShipType());
        dto.put("locations", ship.getShipLocations());
        return dto;
    }



}
