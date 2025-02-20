package knopa.bed_wars.data;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.points.BedPoint;
import knopa.bed_wars.arena.points.capturable.CapturablePoint;
import knopa.bed_wars.arena.points.capturable.PointResource;
import knopa.bed_wars.arena.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {

    public static  DataBaseManager instance = new DataBaseManager();

    private  final Connection connection;
    private final Statement statement;

    private DataBaseManager() {
        try {
            connection = DriverManager.getConnection(Bed_wars.getInstance().getConfig().getString("database_url"));
            statement = connection.createStatement();
            //Создаем таблицу БД
            statement.execute("CREATE TABLE IF NOT EXISTS arenas" +
                    "(" +
                    "name TEXT primary key," +
                    "min_players INTEGER," +
                    "max_players INTEGER," +
                    "center_world_name TEXT," +
                    "center_x INTEGER," +
                    "center_y INTEGER," +
                    "center_z INTEGER" +
                    ")");

            statement.execute("CREATE TABLE IF NOT EXISTS teams" +
                    "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "size INTEGER," +
                    "color TEXT," +
                    "spawn_world_name TEXT," +
                    "spawn_x INTEGER," +
                    "spawn_y INTEGER," +
                    "spawn_z INTEGER," +
                    "bed_point_world_name TEXT," +
                    "bed_point_x INTEGER," +
                    "bed_point_y INTEGER," +
                    "bed_point_z INTEGER," +
                    "arena_name TEXT," +
                    "FOREIGN KEY (arena_name) REFERENCES arenas(name)" +
                    ")");

            statement.execute("CREATE TABLE IF NOT EXISTS points" +
                    "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "location_world_name TEXT," +
                    "location_x INTEGER," +
                    "location_y INTEGER," +
                    "location_z INTEGER," +
                    "hp DOUBLE," +
                    "point_type TEXT," +
                    "team INTEGER," +
                    "FOREIGN KEY (team) REFERENCES teams(id)" +
                    ")");


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Запись БД
    public void createArena(SiegeArena siegeArena){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO arenas VALUES (?, ?, ?, ?, ?, ?, ?)");

            preparedStatement.setString(1, siegeArena.getName());
            preparedStatement.setInt(2, siegeArena.getMinPlayers());
            preparedStatement.setInt(3, siegeArena.getMaxPlayers());
            preparedStatement.setString(4, siegeArena.getCenter().getWorld().getName());
            preparedStatement.setInt(5, siegeArena.getCenter().getBlockX());
            preparedStatement.setInt(6, siegeArena.getCenter().getBlockY());
            preparedStatement.setInt(7, siegeArena.getCenter().getBlockZ());

            preparedStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void createTeam(Team team, String arenaName){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO teams(name, size, color, spawn_world_name, spawn_x, spawn_y, spawn_z, bed_point_world_name, bed_point_x, bed_point_y, bed_point_z, arena_name ) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ? , ? , ? , ?, ?)");
            preparedStatement.setString(1, team.getName());
            preparedStatement.setInt(2, team.getSize());
            preparedStatement.setString(3, team.getColor().name());
            preparedStatement.setString(4, team.getSpawn().getWorld().getName());
            preparedStatement.setInt(5, team.getSpawn().getBlockX());
            preparedStatement.setInt(6, team.getSpawn().getBlockY());
            preparedStatement.setInt(7, team.getSpawn().getBlockZ());
            preparedStatement.setString(8, team.getBedPoint().getLocation().getWorld().getName());
            preparedStatement.setInt(9, team.getBedPoint().getLocation().getBlockX());
            preparedStatement.setInt(10, team.getBedPoint().getLocation().getBlockY());
            preparedStatement.setInt(11, team.getBedPoint().getLocation().getBlockZ());
            preparedStatement.setString(12, arenaName);

            preparedStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPoint(CapturablePoint capturablePoint, String teamName){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO points(location_world_name, location_x, location_y, location_z, hp, point_type, team) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, capturablePoint.getLocation().getWorld().getName());
            preparedStatement.setInt(2, capturablePoint.getLocation().getBlockX());
            preparedStatement.setInt(3, capturablePoint.getLocation().getBlockY());
            preparedStatement.setInt(4, capturablePoint.getLocation().getBlockZ());
            preparedStatement.setDouble(5, capturablePoint.getHp());
            preparedStatement.setString(6, capturablePoint.getType().name());
            preparedStatement.setString(7, teamName);

            preparedStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Читаем из БД
    public List<SiegeArena> getArenas(){
        List<SiegeArena> siegeArenaList = new ArrayList<>();

        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM arenas");

            while (resultSet.next()) {
                SiegeArena siegeArena = new SiegeArena(
                resultSet.getString(1),
                resultSet.getInt(2),
                resultSet.getInt(3),
                new Location(
                        Bukkit.getWorld(resultSet.getString(4)),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getInt(7)
                )
                        );

                ResultSet teamSet = statement.executeQuery("SELECT * FROM teams WHERE arena_name ='" + resultSet.getString(1) + "';");

                while (teamSet.next()) {

                    Team team = new Team(
                            teamSet.getString(2),
                            teamSet.getInt(3),
                            ChatColor.valueOf(teamSet.getString(4)),
                            new Location(
                                    Bukkit.getWorld(teamSet.getString(5)),
                                    teamSet.getInt(6),
                                    teamSet.getInt(7),
                                    teamSet.getInt(8)
                            ),
                            new BedPoint(
                                    new Location(
                                            Bukkit.getWorld(teamSet.getString(9)),
                                            teamSet.getInt(10),
                                            teamSet.getInt(11),
                                            teamSet.getInt(12)
                                    )
                            )

                    );


                    siegeArena.addTeam(team);

                    ResultSet pointSet = statement.executeQuery("SELECT * FROM points WHERE team =" + teamSet.getInt(1));

                    while (pointSet.next()) {
                        team.addPoint(
                                new CapturablePoint(
                                   new Location(
                                           Bukkit.getWorld(pointSet.getString(2)),
                                           pointSet.getInt(3),
                                           pointSet.getInt(4),
                                           pointSet.getInt(5)
                                   ),
                                        pointSet.getDouble(6),
                                        PointResource.valueOf(pointSet.getString(7)),
                                        team
                                )
                        );
                    }
                }

                siegeArena.startGame();
                siegeArenaList.add(siegeArena);


            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return siegeArenaList;
    }
}
