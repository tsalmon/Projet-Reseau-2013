bon ici nous avons les fichiers pour testé la multidifusion et un cafe qui 
peut a la fois ecouter normalement et ecouter en multidifusion sur la meme socket: 

- un sender (client qui envoi un message sur la liste de diffusion) 
      #./sender message
- un receiver (client qui recois un message provenant de la difusion)
      #./receiver
- une ville (qui envoie un message a un café)
      #./ville adresseDuCafe message
- un cafe (qui ecoute les messages a la fois de la liste de difusion et ceux provenant de la ville.
  et ce sur une unique socket.)
      #./cafe


comment utilisé ce test:
- lancer plusieur reciver
- lancer un cafe 
- envoyer un message avec sender (tout les receiver et le cafe le recevra)
- envoyer un message avec la ville (seul le cafe le recevra
