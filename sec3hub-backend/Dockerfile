
FROM denoland/deno:alpine
WORKDIR /app
COPY . .
EXPOSE 8080
ENV PORT=8080
RUN deno cache src/deps.ts
ENTRYPOINT ["deno"]
CMD ["task", "prod"]