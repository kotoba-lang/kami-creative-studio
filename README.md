# KAMI Creative Studio

Open, credential-free creator UI for a CID-linked Murakumo pipeline:

```text
brief / reference
  → 3D model (TRELLIS / Hunyuan3D)
  → humanoid rig (UniRig / explicit heuristic)
  → motion (validated EDN clip on the Mac mini control fleet)
  → music (ACE-Step / Stable Audio)
  → kami.creative-project/v1 manifest
```

Open the public Studio: <https://kotoba-lang.github.io/kami-creative-studio/>

## What is live

- Responsive, accessible project workspace
- Realtime local/generated VRM and GLB preview with camera controls and animation playback
- Integrated `kotoba-lang/kisekae` character edit operations for parts, materials, expressions and motion
- Murakumo progress polling and automatic preview when a generated artifact URL arrives
- Build-generated `KAMI Prism` sample project and embedded-buffer glTF, loaded into the viewer on startup
- Offline project planning and browser-local persistence
- JSON manifest preview, copy and download
- Explicit Murakumo endpoint submission with visible success/failure state
- No credentials, API keys, analytics or third-party scripts
- GitHub Pages deployment on every `main` update

Heavy model, rig and music inference requires a configured remote GPU capability. The Mac mini fleet controls the job graph and owns bounded EDN motion generation/validation; the UI never claims that CUDA-only models execute locally.

## Project contract

The generated document is `kami.creative-project/v1`. Every stage declares its modality, model, input relationship, output kinds and required execution capability. `noSilentFallback` is always true.

## Local development

```sh
npm ci
npm run release
python3 -m http.server 4173 --directory public
```

Then open `http://localhost:4173`. The authored UI is ClojureScript Hiccup/Reagent. Styling is extracted from `shadow.css/css` forms; the GitHub Pages shell is generated from Hiccup during release. There are no hand-authored HTML or CSS files.

The sample source is `resources/samples/kami-sample.edn`. `npm run release` deterministically generates `public/samples/kami-sample.gltf` and its project manifest; generated files are not committed.

## License

MIT
