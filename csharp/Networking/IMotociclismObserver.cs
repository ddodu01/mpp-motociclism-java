using Model;

namespace Networking;

public interface IMotociclismObserver
{
    void InscriereEfectuata(Inscriere inscriere);
    void EchipaModificata(Participant participant);
}