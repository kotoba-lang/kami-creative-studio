(() => {
  "use strict";
  const $ = (id) => document.getElementById(id);
  const storageKey = "kami-creative-studio/project-v1";
  let manifest = null;

  const stageCopy = {
    model: ["3D Geometry", "参照画像とbriefからGLB/glTFモデルを生成します。"],
    rig: ["Humanoid Rig", "生成モデルへVRM humanoid boneとskin weightを付与します。"],
    motion: ["Motion", "リグ済みモデルへ、検証可能なEDN animation clipを適用します。"],
    music: ["Soundtrack", "世界観に沿った音楽を生成し、project artifactへ結びます。"]
  };

  function value(id) { return $(id).value.trim(); }
  function toast(message) {
    $("toast").textContent = message;
    $("toast").classList.add("show");
    window.setTimeout(() => $("toast").classList.remove("show"), 2200);
  }
  function updateMode() {
    const online = Boolean(value("endpoint"));
    $("modeBadge").textContent = online ? "ENDPOINT READY" : "OFFLINE";
    $("modeBadge").className = `badge ${online ? "online" : "offline"}`;
    $("runButton").disabled = !manifest || !online;
    $("statusDetail").textContent = online ? "Ready to submit to the configured endpoint" : "Offline manifest mode";
  }
  function buildManifest() {
    const brief = value("brief");
    if (!brief) { $("brief").focus(); toast("Creative briefを入力してください"); return null; }
    const ref = value("reference");
    const id = `project-${Date.now().toString(36)}`;
    manifest = {
      schema: "kami.creative-project/v1",
      id,
      name: value("projectName") || "Untitled character",
      brief,
      createdAt: new Date().toISOString(),
      stages: [
        { id: "model", modality: "3d", model: "trellis", refs: ref ? [ref] : [], requires: "remote-gpu-capability", output: ["glb", "gltf"] },
        { id: "rig", modality: "rig", model: "unirig", from: "model", requires: "remote-gpu-capability", output: ["vrm"] },
        { id: "motion", modality: "motion", model: "edn-motion-v1", from: "rig", params: { preset: $("motionPreset").value, duration: Number($("duration").value) }, requires: "mac-mini-control-worker", output: ["edn"] },
        { id: "music", modality: "music", model: "ace-step", prompt: brief, requires: "remote-gpu-capability", output: ["wav"] }
      ],
      policy: { noSilentFallback: true, artifactAddressing: "CID", credentialsInManifest: false }
    };
    $("manifestOutput").textContent = JSON.stringify(manifest, null, 2);
    $("projectStatus").textContent = "PLAN READY";
    updateMode();
    toast("制作プランを生成しました");
    return manifest;
  }
  function projectSnapshot() {
    return { projectName: value("projectName"), brief: value("brief"), reference: value("reference"), endpoint: value("endpoint"), motionPreset: $("motionPreset").value, duration: $("duration").value, manifest };
  }
  function save() {
    localStorage.setItem(storageKey, JSON.stringify(projectSnapshot()));
    toast("このブラウザに保存しました");
  }
  function restore() {
    try {
      const state = JSON.parse(localStorage.getItem(storageKey));
      if (!state) return;
      ["projectName", "brief", "reference", "endpoint"].forEach((key) => { if (state[key] != null) $(key).value = state[key]; });
      if (state.motionPreset) $("motionPreset").value = state.motionPreset;
      if (state.duration) $("duration").value = state.duration;
      if (state.manifest) { manifest = state.manifest; $("manifestOutput").textContent = JSON.stringify(manifest, null, 2); $("projectStatus").textContent = "SAVED PLAN"; }
    } catch { localStorage.removeItem(storageKey); }
  }
  function download() {
    if (!manifest && !buildManifest()) return;
    const blob = new Blob([JSON.stringify(manifest, null, 2)], { type: "application/json" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = `${manifest.id}.json`;
    link.click();
    URL.revokeObjectURL(link.href);
    toast("manifestを書き出しました");
  }
  async function submit() {
    if (!manifest && !buildManifest()) return;
    const endpoint = value("endpoint");
    if (!endpoint) { toast("Murakumo endpointを入力してください"); return; }
    $("runButton").disabled = true; $("projectStatus").textContent = "SUBMITTING";
    try {
      const response = await fetch(endpoint, { method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify(manifest) });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const body = await response.text();
      $("projectStatus").textContent = "QUEUED";
      $("statusDetail").textContent = body.slice(0, 180) || "Accepted by Murakumo";
      toast("Murakumoへ送信しました");
    } catch (error) {
      $("projectStatus").textContent = "SUBMIT FAILED";
      $("statusDetail").textContent = error.message;
      toast("送信に失敗しました。endpoint/CORSを確認してください");
    } finally { updateMode(); }
  }

  $("projectForm").addEventListener("submit", (event) => { event.preventDefault(); buildManifest(); });
  $("brief").addEventListener("input", () => { $("briefCount").textContent = $("brief").value.length; });
  $("endpoint").addEventListener("input", updateMode);
  $("duration").addEventListener("input", () => { $("durationValue").textContent = `${$("duration").value}s`; });
  document.querySelectorAll(".stage").forEach((stage) => stage.addEventListener("click", () => {
    document.querySelectorAll(".stage").forEach((item) => item.classList.remove("selected")); stage.classList.add("selected");
    const [title, description] = stageCopy[stage.dataset.stage]; $("inspectorTitle").textContent = title; $("inspectorDescription").textContent = description;
    $("motionControls").hidden = stage.dataset.stage !== "motion";
  }));
  document.querySelectorAll("[data-view]").forEach((button) => button.addEventListener("click", () => {
    document.querySelectorAll("[data-view]").forEach((item) => item.classList.toggle("active", item === button));
    const graph = button.dataset.view === "graph"; $("graphView").hidden = !graph; $("manifestView").hidden = graph;
  }));
  $("saveButton").addEventListener("click", save); $("exportButton").addEventListener("click", download); $("runButton").addEventListener("click", submit);
  $("copyButton").addEventListener("click", async () => { if (!manifest && !buildManifest()) return; await navigator.clipboard.writeText(JSON.stringify(manifest, null, 2)); toast("コピーしました"); });
  restore(); $("briefCount").textContent = $("brief").value.length; $("durationValue").textContent = `${$("duration").value}s`; updateMode();
})();
