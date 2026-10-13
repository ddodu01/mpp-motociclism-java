namespace Networking;

public class MotociclismException : Exception
{
    public MotociclismException(string message) : base(message) { }
    public MotociclismException(string message, Exception inner) : base(message, inner) { }
}