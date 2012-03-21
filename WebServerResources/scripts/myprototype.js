/* Correctif necessaire au bon fonctionnement sous Opera 9.5
 * afin de traiter correctement une reponse serveur avec un status de 0
 * Pour une raison que j'ignore, si le status de la reponse est different de 200, 
 * Opera le positionne a 0 ..... 
*/
Object.extend(Ajax.Request.prototype, {
  success: function() {
    var status = this.getStatus();
    return status && (status >= 200 && status < 300);
  }
})

