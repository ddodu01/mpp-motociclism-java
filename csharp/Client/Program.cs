using System.Windows.Forms;
using log4net;
using log4net.Config;
using Networking;
using System;
using System.Windows.Forms;
using log4net;
using log4net.Config;
using Networking;

namespace Client;

static class Program
{
    private static readonly ILog log = LogManager.GetLogger(typeof(Program));

    [STAThread]
    static void Main()
    {
        XmlConfigurator.Configure(new System.IO.FileInfo("log4net.config"));
        log.Info("Starting application...");

        // Acum conectăm prin server, nu direct la DB
        IMotociclismServices services = new ServerProxy("localhost", 55555);

        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Application.Run(new LoginForm(services));
    }
}