using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SQLite;
using log4net;
using Model;

namespace Repository;

public class UserDbRepository : IUserRepository
{
    private static readonly ILog log = LogManager.GetLogger(typeof(UserDbRepository));
    private readonly string connectionString;

    public UserDbRepository(IDictionary<string, string> props)
    {
        log.Info("Initializing UserDbRepository...");
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

    public User FindOne(long id)
    {
        log.Debug($"Finding user with id {id}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "SELECT * FROM Users WHERE id=@id";
                var param = command.CreateParameter();
                param.ParameterName = "@id";
                param.Value = id;
                command.Parameters.Add(param);

                using (var reader = command.ExecuteReader())
                {
                    if (reader.Read())
                    {
                        var user = new User(
                            reader["username"].ToString(),
                            reader["password"].ToString());
                        user.Id = Convert.ToInt64(reader["id"]);
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public IEnumerable<User> FindAll()
    {
        log.Debug("Finding all users");
        var users = new List<User>();
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "SELECT * FROM Users";
                using (var reader = command.ExecuteReader())
                {
                    while (reader.Read())
                    {
                        var user = new User(
                            reader["username"].ToString(),
                            reader["password"].ToString());
                        user.Id = Convert.ToInt64(reader["id"]);
                        users.Add(user);
                    }
                }
            }
        }
        return users;
    }

    public User Save(User entity)
    {
        log.Debug($"Saving user {entity.Username}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "INSERT INTO Users (username, password) VALUES (@username, @password)";

                var paramUser = command.CreateParameter();
                paramUser.ParameterName = "@username";
                paramUser.Value = entity.Username;
                command.Parameters.Add(paramUser);

                var paramPass = command.CreateParameter();
                paramPass.ParameterName = "@password";
                paramPass.Value = entity.Password;
                command.Parameters.Add(paramPass);

                command.ExecuteNonQuery();
            }
        }
        return entity;
    }

    public User Delete(long id)
    {
        log.Debug($"Deleting user with id {id}");
        var user = FindOne(id);
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "DELETE FROM Users WHERE id=@id";
                var param = command.CreateParameter();
                param.ParameterName = "@id";
                param.Value = id;
                command.Parameters.Add(param);
                command.ExecuteNonQuery();
            }
        }
        return user;
    }

    public User Update(User entity)
    {
        log.Debug($"Updating user {entity.Id}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "UPDATE Users SET username=@username, password=@password WHERE id=@id";

                var paramUser = command.CreateParameter();
                paramUser.ParameterName = "@username";
                paramUser.Value = entity.Username;
                command.Parameters.Add(paramUser);

                var paramPass = command.CreateParameter();
                paramPass.ParameterName = "@password";
                paramPass.Value = entity.Password;
                command.Parameters.Add(paramPass);

                var paramId = command.CreateParameter();
                paramId.ParameterName = "@id";
                paramId.Value = entity.Id;
                command.Parameters.Add(paramId);

                command.ExecuteNonQuery();
            }
        }
        return entity;
    }

    public User FindByUsername(string username)
    {
        log.Debug($"Finding user by username {username}");
        using (var connection = GetConnection())
        {
            connection.Open();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = "SELECT * FROM Users WHERE username=@username";
                var param = command.CreateParameter();
                param.ParameterName = "@username";
                param.Value = username;
                command.Parameters.Add(param);

                using (var reader = command.ExecuteReader())
                {
                    if (reader.Read())
                    {
                        var user = new User(
                            reader["username"].ToString(),
                            reader["password"].ToString());
                        user.Id = Convert.ToInt64(reader["id"]);
                        return user;
                    }
                }
            }
        }
        return null;
    }
}