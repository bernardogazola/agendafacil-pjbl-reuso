# syntax=docker/dockerfile:1.7

# Stage 1: builder
FROM oven/bun:1-alpine AS builder

WORKDIR /app

# Cache deps
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

# Copia o resto e gera o build SSR em dist/.
COPY . .
RUN bun run build

# Stage 2: runtime
FROM oven/bun:1-alpine

WORKDIR /app

COPY --from=builder --chown=bun:bun /app/package.json   ./
COPY --from=builder --chown=bun:bun /app/bun.lock       ./
COPY --from=builder --chown=bun:bun /app/node_modules/  ./node_modules/
COPY --from=builder --chown=bun:bun /app/dist/          ./dist/
COPY --chown=bun:bun server.ts                          ./

USER bun

ENV PORT=3000
ENV HOST=0.0.0.0
ENV NODE_ENV=production

EXPOSE 3000

CMD ["bun", "run", "server.ts"]