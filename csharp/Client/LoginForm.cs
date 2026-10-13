using System;
using System.Windows.Forms;
using Model;
using Networking;

namespace Client;

public class LoginForm : Form
{
    private Label labelUsername;
    private Label labelPassword;
    private TextBox textUsername;
    private TextBox textPassword;
    private Button buttonLogin;
    private Label labelError;

    private readonly IMotociclismServices _services;

    public LoginForm(IMotociclismServices services)
    {
        _services = services;
        InitializeComponents();
    }

    private void InitializeComponents()
    {
        this.Text = "Motociclism - Login";
        this.Width = 350;
        this.Height = 250;
        this.StartPosition = FormStartPosition.CenterScreen;

        labelUsername = new Label { Text = "Username:", Left = 30, Top = 30, Width = 100 };
        textUsername = new TextBox { Left = 140, Top = 30, Width = 150 };

        labelPassword = new Label { Text = "Parola:", Left = 30, Top = 70, Width = 100 };
        textPassword = new TextBox { Left = 140, Top = 70, Width = 150, PasswordChar = '*' };

        buttonLogin = new Button { Text = "Login", Left = 130, Top = 110, Width = 80 };
        buttonLogin.Click += HandleLogin;

        labelError = new Label { Left = 30, Top = 150, Width = 280, ForeColor = System.Drawing.Color.Red };

        this.Controls.Add(labelUsername);
        this.Controls.Add(textUsername);
        this.Controls.Add(labelPassword);
        this.Controls.Add(textPassword);
        this.Controls.Add(buttonLogin);
        this.Controls.Add(labelError);
    }

    private void HandleLogin(object sender, EventArgs e)
    {
        string username = textUsername.Text;
        string password = textPassword.Text;

        if (string.IsNullOrEmpty(username) || string.IsNullOrEmpty(password))
        {
            labelError.Text = "Introduceti username si parola!";
            return;
        }

        try
        {
            var mainForm = new MainForm(_services, username, password);
            mainForm.Show();
            this.Hide();
        }
        catch (MotociclismException ex)
        {
            labelError.Text = ex.Message;
        }
    }
}