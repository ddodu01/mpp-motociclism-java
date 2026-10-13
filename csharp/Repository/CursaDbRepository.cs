using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SQLite;
using log4net;
using Model;

namespace Repository;

public class CursaDbRepository : ICursaRepository
{
    private static readonly ILog log = LogManager.GetLogger(typeof(CursaDbRepository));
    private readonly string connectionString;

    public CursaDbRepository(IDictionary<string, string> props)
    {
        log.Info("Initializing CursaDbRepository...");
        if (props.ContainsKey("ConnectionString"))
            connectionString = props["ConnectionString"];
        else
        {
            log.Error("ConnectionString is missing from properties!");
            throw new ArgumentException("ConnectionString is missing");
        }
    }

    private IDbConnection GetConnection()
    {
        return new SQLiteConnection(connectionString);
    }

    public Cursa FindOne(long id)
    {
        log.Debug($"Finding cursa with id {id}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "SELECT * FROM Curse WHERE id=@id";
                var param = command.CreateParameter();
                param.ParameterName = "@id";
                param.Value = id;
                command.Parameters.Add(param);

                using (var reader = command.ExecuteReader())
                {
                    if (reader.Read())
                    {
                        var cursa = new Cursa(Convert.ToInt32(reader["capacitateMotor"]));
                        cursa.Id = Convert.ToInt64(reader["id"]);
                        return cursa;
                    }
                }
            }
        }
        return null;
    }

    public IEnumerable<Cursa> FindAll()
    {
        log.Debug("Finding all curse");
        var curse = new List<Cursa>();
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "SELECT * FROM Curse";
                using (var reader = command.ExecuteReader())
                {
                    while (reader.Read())
                    {
                        var cursa = new Cursa(Convert.ToInt32(reader["capacitateMotor"]));
                        cursa.Id = Convert.ToInt64(reader["id"]);
                        curse.Add(cursa);
                    }
                }
            }
        }
        return curse;
    }

    public Cursa Save(Cursa entity)
    {
        log.Debug($"Saving cursa {entity.CapacitateMotor}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "INSERT INTO Curse (capacitateMotor) VALUES (@capacitateMotor)";
                var param = command.CreateParameter();
                param.ParameterName = "@capacitateMotor";
                param.Value = entity.CapacitateMotor;
                command.Parameters.Add(param);
                command.ExecuteNonQuery();
            }
        }
        return entity;
    }

    public Cursa Delete(long id)
    {
        log.Debug($"Deleting cursa with id {id}");
        var cursa = FindOne(id);
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "DELETE FROM Curse WHERE id=@id";
                var param = command.CreateParameter();
                param.ParameterName = "@id";
                param.Value = id;
                command.Parameters.Add(param);
                command.ExecuteNonQuery();
            }
        }
        return cursa;
    }

    public Cursa Update(Cursa entity)
    {
        log.Debug($"Updating cursa {entity.Id}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "UPDATE Curse SET capacitateMotor=@capacitateMotor WHERE id=@id";

                var paramCap = command.CreateParameter();
                paramCap.ParameterName = "@capacitateMotor";
                paramCap.Value = entity.CapacitateMotor;
                command.Parameters.Add(paramCap);

                var paramId = command.CreateParameter();
                paramId.ParameterName = "@id";
                paramId.Value = entity.Id;
                command.Parameters.Add(paramId);

                command.ExecuteNonQuery();
            }
        }
        return entity;
    }
}