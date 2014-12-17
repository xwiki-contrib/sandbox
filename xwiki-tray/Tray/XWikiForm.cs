using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.IO;
using System.Drawing;
using System.Diagnostics;
using System.Text;
using System.Windows.Forms;
using System.Collections.Specialized;
using System.Threading;

namespace Tray
{
    public partial class XWikiForm : Form
    {
        Process proc;
        String consoleBuffer ="";
        bool canClose = false;
        public XWikiForm()
        {
            InitializeComponent();
        }

        void SetupRedirect(Object path)
        {
            String cmdPath = (String)path;
            proc = new Process();
            // set up output redirection
            proc.StartInfo.RedirectStandardOutput = true;
            proc.StartInfo.RedirectStandardError = true;
            proc.StartInfo.UseShellExecute = false;
            proc.EnableRaisingEvents = true;
            proc.StartInfo.CreateNoWindow = true;
            ///
            StringDictionary envVar = proc.StartInfo.EnvironmentVariables;
            envVar.Add("LANG", "fr_FR.ISO8859-1");
            envVar.Add("JETTY_HOME", "jetty");
            envVar.Add("SERVER_PATH", cmdPath);
            envVar.Add("JETTY_PORT", "8080");
            envVar.Add("JAVA_OPTS", "-Xmx301m");
            String args = envVar["JAVA_OPTS"] + " ";
            args += "-Dfile.encoding=iso-8859-1" + " ";
            args += "-Djetty.home=" + envVar["JETTY_HOME"] + " ";
            args += "-Djetty.port=" + envVar["JETTY_PORT"] + " ";
            args += "-jar " + "\"" + envVar["SERVER_PATH"] + "\"";
            proc.StartInfo.WorkingDirectory = Path.GetDirectoryName(cmdPath);
            proc.StartInfo.FileName = "java";
            proc.StartInfo.Arguments = args;
            // see below for output handler
            proc.ErrorDataReceived += proc_DataReceived;
            proc.OutputDataReceived += proc_DataReceived;     

        }

        void proc_DataReceived(object sender, DataReceivedEventArgs e)
        {
            // output will be in string e.Data
            consoleBuffer += Environment.NewLine + e.Data;
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            if(consoleBuffer.Contains("SelectChannelConnector"))
            {
                Process p = new Process();
                p.StartInfo = new ProcessStartInfo("httl://localhost:8080");
            }
            if (consoleBuffer.Length > 0)
            {
                richTextBox1.Text += consoleBuffer;
            }
            consoleBuffer = "";
            richTextBox1.Font = new Font(FontFamily.GenericMonospace, 10);
        }

        private void XWiki_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            this.Show();
            WindowState = FormWindowState.Normal;
        }

        private void Form1_Resize(object sender, EventArgs e)
        {
            if (FormWindowState.Minimized == WindowState)
            {
                Hide();
            }
        }

        private void stopXWikiToolStripMenuItem_Click(object sender, EventArgs e)
        {
            try
            {
                proc.Kill();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
            startXWikiToolStripMenuItem.Enabled = true;
            tray.ShowBalloonTip(2000, "XWiki", "XWiki server has been stopped.", ToolTipIcon.Info);
        }

        private void startXWikiToolStripMenuItem_Click(object sender, EventArgs e)
        {
            StartXWikiInNewThread();
        }

        private void StartXWikiInNewThread()
        {
            Thread t = new Thread(new ThreadStart(RunXWiki));
            t.Start();
            tray.ShowBalloonTip(2000, "XWiki", "XWiki server has started.", ToolTipIcon.Info);
            stopXWikiToolStripMenuItem.Enabled = true;
        }

        private void RunXWiki()
        {
            proc.Start();
            try
            {
                proc.BeginErrorReadLine();
                proc.BeginOutputReadLine();
                proc.WaitForExit();
            }
            catch (Exception ex) { };
        }

        private void chooseServerToolStripMenuItem_Click(object sender, EventArgs e)
        {
            OpenFileDialog dialog = new OpenFileDialog();
            if (dialog.ShowDialog() == DialogResult.OK)
            {
                String path = dialog.FileName;
                timer1.Start();
                SetupRedirect(path);
            }
            StartXWikiInNewThread();
        }

        private void XWikiForm_Load(object sender, EventArgs e)
        {
            this.Hide();
            tray.ShowBalloonTip(500, "XWiki", "XWiki server tray.", ToolTipIcon.Info);
        }

        private void showConsoleToolStripMenuItem_Click(object sender, EventArgs e)
        {
            this.Show();
            this.WindowState = FormWindowState.Normal;
        }

        private void XWikiForm_FormClosed(object sender, FormClosedEventArgs e)
        {
            
        }

        private void XWikiForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (!canClose)
            {
                this.Hide();
                e.Cancel = true;
            }
        }

        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (proc != null)
            {
                proc.Kill();
            }
            canClose = true;
            tray.Visible = false;
            this.Close();
            Application.Exit();
        }
    }
}
