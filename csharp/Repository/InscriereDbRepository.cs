using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SQLite;
using log4net;
using Model;

namespace Repository;

public class InscriereDbRepository : IInscriereRepository
{
    private static readonly ILog log = LogManager.GetLogger(typeof(InscriereDbRepository));
    private readonly string connectionString;
    private readonly ICursaRepository cursaRepository;
    private readonly IParticipantRepository participantRepository;

    public InscriereDbRepository(IDictionary<string, string> props,
        ICursaRepository cursaRepository,
        IParticipantRepository participantRepository)
    {
        log.Info("Initializing InscriereDbRepository...");
        if (props.ContainsKey("ConnectionString"))
            connectionString = props["ConnectionString"];
        else
        {
            log.Error("ConnectionString is missing from properties!");
            throw new ArgumentException("ConnectionString is missing");
        }
        this.cursaRepository = cursaRepository;
        this.participantRepository = participantRepository;
    }

    private IDbConnection GetConnection()
    {
        return new SQLiteConnection(connectionString);
    }

    public Inscriere FindOne(long id)
    {
        log.Debug($"Finding inscriere with id {id}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "SELECT * FROM Inscrieri WHERE id=@id";
                var param = command.CreateParameter();
                param.ParameterName = "@id";
                param.Value = id;
                command.Parameters.Add(param);

                using (var reader = command.ExecuteReader())
                {
                    if (reader.Read())
                    {
                        var cursa = cursaRepository.FindOne(Convert.ToInt64(reader["cursaId"]));
                        var participant = participantRepository.FindOne(Convert.ToInt64(reader["participantId"]));
                        var inscriere = new Inscriere(cursa, participant);
                        inscriere.Id = Convert.ToInt64(reader["id"]);
                        return inscriere;
                    }
                }
            }
        }
        return null;
    }

    public IEnumerable<Inscriere> FindAll()
    {
        log.Debug("Finding all inscrieri");
        var inscrieri = new List<Inscriere>();
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "SELECT * FROM Inscrieri";
                using (var reader = command.ExecuteReader())
                {
                    while (reader.Read())
                    {
                        var cursa = cursaRepository.FindOne(Convert.ToInt64(reader["cursaId"]));
                        var participant = participantRepository.FindOne(Convert.ToInt64(reader["participantId"]));
                        var inscriere = new Inscriere(cursa, participant);
                        inscriere.Id = Convert.ToInt64(reader["id"]);
                        inscrieri.Add(inscriere);
                    }
                }
            }
        }
        return inscrieri;
    }

    public Inscriere Save(Inscriere entity)
    {
        log.Debug("Saving inscriere");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "INSERT INTO Inscrieri (cursaId, participantId) VALUES (@cursaId, @participantId)";

                var paramCursa = command.CreateParameter();
                paramCursa.ParameterName = "@cursaId";
                paramCursa.Value = entity.Cursa.Id;
                command.Parameters.Add(paramCursa);

                var paramParticipant = command.CreateParameter();
                paramParticipant.ParameterName = "@participantId";
                paramParticipant.Value = entity.Participant.Id;
                command.Parameters.Add(paramParticipant);

                command.ExecuteNonQuery();
            }
        }
        return entity;
    }

    public Inscriere Delete(long id)
    {
        log.Debug($"Deleting inscriere with id {id}");
        var inscriere = FindOne(id);
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "DELETE FROM Inscrieri WHERE id=@id";
                var param = command.CreateParameter();
                param.ParameterName = "@id";
                param.Value = id;
                command.Parameters.Add(param);
                command.ExecuteNonQuery();
            }
        }
        return inscriere;
    }

    public Inscriere Update(Inscriere entity)
    {
        log.Debug($"Updating inscriere {entity.Id}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "UPDATE Inscrieri SET cursaId=@cursaId, participantId=@participantId WHERE id=@id";

                var paramCursa = command.CreateParameter();
                paramCursa.ParameterName = "@cursaId";
                paramCursa.Value = entity.Cursa.Id;
                command.Parameters.Add(paramCursa);

                var paramParticipant = command.CreateParameter();
                paramParticipant.ParameterName = "@participantId";
                paramParticipant.Value = entity.Participant.Id;
                command.Parameters.Add(paramParticipant);

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