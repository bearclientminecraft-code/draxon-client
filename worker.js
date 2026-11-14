export default {
  async fetch(request) {
    const url = new URL(request.url);
    const assetPath = url.pathname.startsWith('/') ? url.pathname.slice(1) : url.pathname;
    try {
      const resp = await env.ASSETS.fetch(request);
      if (resp.status !== 404) return resp;
    } catch (e) {}
    return new Response('Not found', { status: 404 });
  }
};
