using System.Collections.Generic;
using Model;

namespace Repository;

public interface IParticipantRepository : IRepository<long, Participant>
{
    IEnumerable<Participant> FindByEchipa(string echipa);
}