using Model;

namespace Repository;

public interface IUserRepository : IRepository<long, User>
{
    User FindByUsername(string username);
}