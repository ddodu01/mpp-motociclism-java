using System;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;
using Model;
using Networking;

namespace Client;

public class MainForm : Form, IMotociclismObserver
{
    private readonly IMotociclismServices _services;
    private User _loggedUser;

    private DataGridView cursaGrid;
    private DataGridView participantGrid;
    private TextBox textEchipa;
    private Button buttonCauta;
    private TextBox textNume;
    private TextBox textCnp;
    private TextBox textEchipaInscriere;
    private TextBox textCursaId;
    private Button buttonInscrie;
    private TextBox textParticipantId;
    private TextBox textEchipaNoua;
    private Button buttonModifica;
    private Label labelStatus;
    private Button buttonLogout;

    public MainForm(IMotociclismServices services, string username, string password)
    {
        _services = services;
        InitializeComponents();

        // Login — îl facem aici și pasăm this ca observer
        _loggedUser = _services.Login(username, password, this);
        Task.Run(() =>
        {
            var curse = _services.GetCurse().ToList();
            this.Invoke(() =>
            {
                cursaGrid.Rows.Clear();
                foreach (var cursa in curse)
                    cursaGrid.Rows.Add(cursa.Id, cursa.CapacitateMotor);
            });
        });
    }

    private void InitializeComponents()
    {
        this.Text = "Motociclism";
        this.Width = 700;
        this.Height = 700;
        this.StartPosition = FormStartPosition.CenterScreen;

        int top = 10;

        var labelCurse = new Label
        {
            Text = "Curse disponibile:", Left = 10, Top = top, Width = 200,
            Font = new System.Drawing.Font("Arial", 10, System.Drawing.FontStyle.Bold)
        };
        top += 25;

        cursaGrid = new DataGridView
        {
            Left = 10, Top = top, Width = 660, Height = 150, ReadOnly = true,
            AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill
        };
        cursaGrid.Columns.Add("Id", "ID");
        cursaGrid.Columns.Add("CapacitateMotor", "Capacitate Motor");
        top += 160;

        var labelCauta = new Label
        {
            Text = "Cauta participanti dupa echipa:", Left = 10, Top = top, Width = 300,
            Font = new System.Drawing.Font("Arial", 10, System.Drawing.FontStyle.Bold)
        };
        top += 25;

        textEchipa = new TextBox { Left = 10, Top = top, Width = 200, PlaceholderText = "Nume echipa" };
        buttonCauta = new Button { Text = "Cauta", Left = 220, Top = top, Width = 80 };
        buttonCauta.Click += HandleCauta;
        top += 35;

        participantGrid = new DataGridView
        {
            Left = 10, Top = top, Width = 660, Height = 120, ReadOnly = true,
            AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill
        };
        participantGrid.Columns.Add("Id", "ID");
        participantGrid.Columns.Add("Nume", "Nume");
        participantGrid.Columns.Add("Echipa", "Echipa");
        top += 130;

        var labelInscriere = new Label
        {
            Text = "Inscriere participant nou:", Left = 10, Top = top, Width = 300,
            Font = new System.Drawing.Font("Arial", 10, System.Drawing.FontStyle.Bold)
        };
        top += 25;

        textNume = new TextBox { Left = 10, Top = top, Width = 150, PlaceholderText = "Nume" };
        textCnp = new TextBox { Left = 170, Top = top, Width = 150, PlaceholderText = "CNP" };
        textEchipaInscriere = new TextBox { Left = 330, Top = top, Width = 150, PlaceholderText = "Echipa" };
        top += 35;

        textCursaId = new TextBox { Left = 10, Top = top, Width = 150, PlaceholderText = "ID Cursa" };
        buttonInscrie = new Button { Text = "Inscrie", Left = 170, Top = top, Width = 80 };
        buttonInscrie.Click += HandleInscrie;
        top += 45;

        var labelModifica = new Label
        {
            Text = "Modifica echipa participant:", Left = 10, Top = top, Width = 300,
            Font = new System.Drawing.Font("Arial", 10, System.Drawing.FontStyle.Bold)
        };
        top += 25;

        textParticipantId = new TextBox { Left = 10, Top = top, Width = 150, PlaceholderText = "ID Participant" };
        textEchipaNoua = new TextBox { Left = 170, Top = top, Width = 150, PlaceholderText = "Echipa noua" };
        buttonModifica = new Button { Text = "Modifica", Left = 330, Top = top, Width = 80 };
        buttonModifica.Click += HandleModifica;
        top += 45;

        labelStatus = new Label { Left = 10, Top = top, Width = 500 };
        top += 30;

        buttonLogout = new Button { Text = "Logout", Left = 10, Top = top, Width = 80 };
        buttonLogout.Click += HandleLogout;

        this.Controls.AddRange(new Control[]
        {
            labelCurse, cursaGrid,
            labelCauta, textEchipa, buttonCauta, participantGrid,
            labelInscriere, textNume, textCnp, textEchipaInscriere,
            textCursaId, buttonInscrie,
            labelModifica, textParticipantId, textEchipaNoua, buttonModifica,
            labelStatus, buttonLogout
        });
    }

    private void LoadCurse()
    {
        var curse = _services.GetCurse().ToList();
        cursaGrid.Rows.Clear();
        foreach (var cursa in curse)
            cursaGrid.Rows.Add(cursa.Id, cursa.CapacitateMotor);
    }

    private async void HandleCauta(object sender, EventArgs e)
    {
        string echipa = textEchipa.Text;
        if (string.IsNullOrEmpty(echipa))
        {
            SetStatus("Introduceti numele echipei!", false);
            return;
        }

        try
        {
            buttonCauta.Enabled = false;
            var participanti = await Task.Run(() =>
                _services.CautaParticipanti(echipa).ToList());

            participantGrid.Rows.Clear();
            foreach (var p in participanti)
                participantGrid.Rows.Add(p.Id, p.Nume, p.Echipa);

            SetStatus("Cautare efectuata!", true);
        }
        finally
        {
            buttonCauta.Enabled = true;
        }
    }

    private async void HandleInscrie(object sender, EventArgs e)
    {
        string nume = textNume.Text;
        string cnp = textCnp.Text;
        string echipa = textEchipaInscriere.Text;
        string cursaIdText = textCursaId.Text;

        if (string.IsNullOrEmpty(nume) || string.IsNullOrEmpty(cnp) || string.IsNullOrEmpty(cursaIdText))
        {
            SetStatus("Completati toate campurile!", false);
            return;
        }

        if (!long.TryParse(cursaIdText, out long cursaId))
        {
            SetStatus("ID cursa invalid!", false);
            return;
        }

        try
        {
            buttonInscrie.Enabled = false;
            await Task.Run(() => _services.Inscrie(nume, cnp, echipa, cursaId));
            SetStatus("Participant inscris cu succes!", true);
        }
        catch (MotociclismException ex)
        {
            SetStatus(ex.Message, false);
        }
        finally
        {
            buttonInscrie.Enabled = true;
        }
    }

    private async void HandleModifica(object sender, EventArgs e)
    {
        string idText = textParticipantId.Text;
        string echipaNoua = textEchipaNoua.Text;

        if (string.IsNullOrEmpty(idText) || string.IsNullOrEmpty(echipaNoua))
        {
            SetStatus("Completati ID-ul si echipa noua!", false);
            return;
        }

        if (!long.TryParse(idText, out long id))
        {
            SetStatus("ID participant invalid!", false);
            return;
        }

        try
        {
            buttonModifica.Enabled = false;
            await Task.Run(() => _services.ModificaEchipa(id, echipaNoua));
            SetStatus("Echipa modificata cu succes!", true);
        }
        catch (MotociclismException ex)
        {
            SetStatus(ex.Message, false);
        }
        finally
        {
            buttonModifica.Enabled = true;
        }
    }

    private void HandleLogout(object sender, EventArgs e)
    {
        try
        {
            _services.Logout(_loggedUser);
        }
        catch
        {
        }

        var loginForm = new LoginForm(_services);
        loginForm.Show();
        this.Close();
    }

    private void SetStatus(string message, bool success)
    {
        labelStatus.Text = message;
        labelStatus.ForeColor = success ? System.Drawing.Color.Green : System.Drawing.Color.Red;
    }

    // --- IMotociclismObserver --- notificări de la server

    public void InscriereEfectuata(Inscriere inscriere)
    {
        this.Invoke(() =>
        {
            Console.WriteLine($"[DEBUG] Echipa participant: '{inscriere.Participant?.Echipa}'");
            Console.WriteLine($"[DEBUG] Echipa cautata: '{textEchipa.Text}'");
            SetStatus($"Participant nou inscris: {inscriere.Participant?.Nume} la cursa {inscriere.Cursa?.Id}!", true);
        
            // Reincarcam cursele pe thread separat
            Task.Run(() => {
                var curse = _services.GetCurse().ToList();
                this.Invoke(() => {
                    cursaGrid.Rows.Clear();
                    foreach (var cursa in curse)
                        cursaGrid.Rows.Add(cursa.Id, cursa.CapacitateMotor);
                });
            });

            // Daca echipa participantului nou e aceeasi cu cea cautata, reincarcam lista
            string echipaCautata = textEchipa.Text;
            if (!string.IsNullOrEmpty(echipaCautata) &&
                inscriere.Participant?.Echipa == echipaCautata)
            {
                Task.Run(() => {
                    var participanti = _services.CautaParticipanti(echipaCautata).ToList();
                    this.Invoke(() => {
                        participantGrid.Rows.Clear();
                        foreach (var p in participanti)
                            participantGrid.Rows.Add(p.Id, p.Nume, p.Echipa);
                    });
                });
            }
        });
    }

    public void EchipaModificata(Participant participant)
    {
        this.Invoke(() =>
        {
            SetStatus($"Echipa participantului {participant.Nume} modificata: {participant.Echipa}!", true);
            // Reincarcam participantii pe un thread separat
            string echipa = textEchipa.Text;
            if (!string.IsNullOrEmpty(echipa))
            {
                Task.Run(() => {
                    var participanti = _services.CautaParticipanti(echipa).ToList();
                    this.Invoke(() => {
                        participantGrid.Rows.Clear();
                        foreach (var p in participanti)
                            participantGrid.Rows.Add(p.Id, p.Nume, p.Echipa);
                    });
                });
            }
        });
    }
}    