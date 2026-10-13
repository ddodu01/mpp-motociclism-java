using System;

namespace Model;

[Serializable]
public class Participant : Entity<long>
{
    public string Nume { get; set; }
    public string Cnp { get; set; }
    public string Echipa { get; set; }
    
    public Participant() { }
    public Participant(string nume, string cnp, string echipa)
    {
        Nume = nume;
        Cnp = cnp;
        Echipa = echipa;
    }
}