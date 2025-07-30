# Étape 1 : Build Angular app
FROM node:22 AS build-stage

WORKDIR /app

COPY . .

RUN npm install && npm run build --prod

FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build-stage /app/dist/itsm-ui/browser /usr/share/nginx/html

# Copier un fichier custom nginx.conf (optionnel, sinon laisser par défaut)
 COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
