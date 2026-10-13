using System;

namespace Model;

[Serializable]
public class Inscriere : Entity<long>
{
    public Cursa Cursa { get; set; }
    public Participant Participant { get; set; }
    
    public Inscriere() { }
    public Inscriere(Cursa cursa, Participant participant)
    {
        Cursa = cursa;
        Participant = participant;
    }
}