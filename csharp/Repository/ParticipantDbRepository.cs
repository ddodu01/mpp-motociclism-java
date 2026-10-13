using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SQLite;
using log4net;
using Model;

namespace Repository;

public class ParticipantDbRepository : IParticipantRepository
{
    // Inițializăm logger-ul specific pentru această clasă
    private static readonly ILog log = LogManager.GetLogger(typeof(ParticipantDbRepository));
    
    private readonly string connectionString;

    // Folosim un dicționar pentru a prelua proprietățile de configurare (similar cu java.util.Properties)
    public ParticipantDbRepository(IDictionary<string, string> props)
    {
        log.Info("Initializing ParticipantDbRepository...");
        
        if (props.ContainsKey("ConnectionString"))
        {
            connectionString = props["ConnectionString"];
        }
        else
        {
            log.Error("ConnectionString is missing from properties!");
            throw new ArgumentException("ConnectionString is missing");
        }
    }

    // Metoda privată pentru a obține o conexiune nouă la SQLite
    private IDbConnection GetConnection()
    {
        log.Debug("Getting database connection");
        return new SQLiteConnection(connectionString);
    }

    public Participant Save(Participant entity)
    {
        log.Debug($"Saving participant {entity.Nume}");
        
        // 'using' asigură închiderea automată a conexiunii (echivalentul try-with-resources din Java)
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                // Setăm comanda SQL cu parametri (@nume, @cnp, @echipa) pentru a preveni SQL Injection
                command.CommandText = "insert into Participanti (nume, cnp, echipa) values (@nume, @cnp, @echipa)";
                
                var paramNume = command.CreateParameter();
                paramNume.ParameterName = "@nume";
                paramNume.Value = entity.Nume;
                command.Parameters.Add(paramNume);

                var paramCnp = command.CreateParameter();
                paramCnp.ParameterName = "@cnp";
                paramCnp.Value = entity.Cnp;
                command.Parameters.Add(paramCnp);

                var paramEchipa = command.CreateParameter();
                paramEchipa.ParameterName = "@echipa";
                // Dacă echipa este null, introducem DBNull.Value în baza de date
                paramEchipa.Value = entity.Echipa ?? (object)DBNull.Value;
                command.Parameters.Add(paramEchipa);

                var result = command.ExecuteNonQuery();
                log.Debug($"Saved {result} instances");
            }
        }
        return entity;
    }

    public Participant FindOne(long id)
{
    log.Debug($"Finding participant with id {id}");
    using (var connection = GetConnection())
    {
        connection.Open();
        using (var command = connection.CreateCommand())
        {
            command.CommandText = "SELECT * FROM Participanti WHERE id=@id";
            var param = command.CreateParameter();
            param.ParameterName = "@id";
            param.Value = id;
            command.Parameters.Add(param);

            using (var reader = command.ExecuteReader())
            {
                if (reader.Read())
                {
                    var p = new Participant(
                        reader["nume"].ToString(),
                        reader["cnp"].ToString(),
                        reader["echipa"].ToString());
                    p.Id = Convert.ToInt64(reader["id"]);
                    log.Debug($"Found participant {p.Nume}");
                    return p;
                }
            }
        }
    }
    return null;
}

public IEnumerable<Participant> FindAll()
{
    log.Debug("Finding all participants");
    var participanti = new List<Participant>();
    using (var connection = GetConnection())
    {
        connection.Open();
        using (var command = connection.CreateCommand())
        {
            command.CommandText = "SELECT * FROM Participanti";
            using (var reader = command.ExecuteReader())
            {
                while (reader.Read())
                {
                    var p = new Participant(
                        reader["nume"].ToString(),
                        reader["cnp"].ToString(),
                        reader["echipa"].ToString());
                    p.Id = Convert.ToInt64(reader["id"]);
                    participanti.Add(p);
                }
            }
        }
    }
    return participanti;
}

public Participant Delete(long id)
{
    log.Debug($"Deleting participant with id {id}");
    var participant = FindOne(id);
    using (var connection = GetConnection())
    {
        connection.Open();
        using (var command = connection.CreateCommand())
        {
            command.CommandText = "DELETE FROM Participanti WHERE id=@id";
            var param = command.CreateParameter();
            param.ParameterName = "@id";
            param.Value = id;
            command.Parameters.Add(param);
            command.ExecuteNonQuery();
        }
    }
    return participant;
}

public Participant Update(Participant entity)
{
    log.Debug($"Updating participant {entity.Id}");
    using (var connection = GetConnection())
    {
        connection.Open();
        using (var command = connection.CreateCommand())
        {
            command.CommandText = "UPDATE Participanti SET nume=@nume, cnp=@cnp, echipa=@echipa WHERE id=@id";

            var paramNume = command.CreateParameter();
            paramNume.ParameterName = "@nume";
            paramNume.Value = entity.Nume;
            command.Parameters.Add(paramNume);

            var paramCnp = command.CreateParameter();
            paramCnp.ParameterName = "@cnp";
            paramCnp.Value = entity.Cnp;
            command.Parameters.Add(paramCnp);

            var paramEchipa = command.CreateParameter();
            paramEchipa.ParameterName = "@echipa";
            paramEchipa.Value = entity.Echipa ?? (object)DBNull.Value;
            command.Parameters.Add(paramEchipa);

            var paramId = command.CreateParameter();
            paramId.ParameterName = "@id";
            paramId.Value = entity.Id;
            command.Parameters.Add(paramId);

            command.ExecuteNonQuery();
        }
    }
    return entity;
}

public IEnumerable<Participant> FindByEchipa(string echipa)
{
    log.Debug($"Finding participants by echipa {echipa}");
    var participanti = new List<Participant>();
    using (var connection = GetConnection())
    {
        connection.Open();
        using (var command = connection.CreateCommand())
        {
            command.CommandText = "SELECT * FROM Participanti WHERE echipa=@echipa";
            var param = command.CreateParameter();
            param.ParameterName = "@echipa";
            param.Value = echipa;
            command.Parameters.Add(param);

            using (var reader = command.ExecuteReader())
            {
                while (reader.Read())
                {
                    var p = new Participant(
                        reader["nume"].ToString(),
                        reader["cnp"].ToString(),
                        reader["echipa"].ToString());
                    p.Id = Convert.ToInt64(reader["id"]);
                    participanti.Add(p);
                }
            }
        }
    }
    return participanti;
}
}