using System;

namespace Model;

[Serializable]
public class Cursa : Entity<long>
{
    public int CapacitateMotor { get; set; }
    
    public Cursa() { }
    public Cursa(int capacitateMotor)
    {
        CapacitateMotor = capacitateMotor;
    }
}