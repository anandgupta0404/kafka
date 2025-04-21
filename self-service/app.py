from flask import Flask, request, jsonify, render_template_string
import subprocess

app = Flask(__name__)

# HTML content
html_page = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Shell Command Runner</title>
  <style>
    body {
      background: linear-gradient(to right, #2c5364, #203a43, #0f2027);
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      color: #ffffff;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px;
    }
    h1 {
      font-size: 2.5rem;
      margin-bottom: 20px;
      color: #00e6e6;
    }
    label, select, button {
      font-size: 1rem;
      margin: 10px;
    }
    select, button {
      padding: 10px 20px;
      border-radius: 8px;
      border: none;
    }
    select {
      background-color: #ffffff;
      color: #000000;
    }
    button {
      background-color: #00b3b3;
      color: white;
      cursor: pointer;
    }
    button:hover {
      background-color: #009999;
    }
    #status {
      margin-top: 10px;
      font-size: 1rem;
      color: #ffcc00;
    }
    .spinner {
      border: 4px solid rgba(255, 255, 255, 0.2);
      border-top: 4px solid #00ffff;
      border-radius: 50%;
      width: 30px;
      height: 30px;
      animation: spin 1s linear infinite;
      display: none;
      margin: 10px auto;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    pre {
      margin-top: 20px;
      padding: 20px;
      background-color: #1f2b3a;
      border-radius: 10px;
      width: 80%;
      max-width: 700px;
      white-space: pre-wrap;
      word-wrap: break-word;
      box-shadow: 0px 0px 10px #00ffff44;
    }
  </style>
</head>
<body>
  <h1>Self service for infrastructure</h1>
  <label for="command">Select a Services</label>
  <select id="command">
    <option value="act --workflows './deploy/.github/workflows/argo-deploy.yml' --container-architecture linux/amd64">kafka-for-k8s</option>
    <option value="pwd">Azure Virtual Machine</option>
    <option value="whoami">Network Services</option>
  </select>
  <button onclick="runCommand()">Create</button>

  <div id="status">Idle</div>
  <div class="spinner" id="spinner"></div>
  <pre id="output"></pre>

  <script>
    async function runCommand() {
      const cmd = document.getElementById('command').value;
      const output = document.getElementById('output');
      const status = document.getElementById('status');
      const spinner = document.getElementById('spinner');

      // Reset and show status
      output.textContent = '';
      status.textContent = 'Running...';
      spinner.style.display = 'block';

      try {
        const response = await fetch('/run', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({command: cmd})
        });

        const data = await response.json();
        output.textContent = data.output || data.error;
        status.textContent = 'Done ✅';
      } catch (err) {
        output.textContent = 'Error: Could not contact server.';
        status.textContent = 'Failed ❌';
      }

      spinner.style.display = 'none';
    }
  </script>
</body>
</html>
"""




@app.route('/')
def index():
    return render_template_string(html_page)

@app.route('/run', methods=['POST'])
def run_command():
    data = request.json
    command = data.get("command")

    try:
        # WARNING: this is unsafe for untrusted input!
        result = subprocess.check_output(command, shell=True, stderr=subprocess.STDOUT, text=True)
        return jsonify({"output": result})
    except subprocess.CalledProcessError as e:
        return jsonify({"error": e.output}), 400

if __name__ == '__main__':
    app.run(debug=True)
