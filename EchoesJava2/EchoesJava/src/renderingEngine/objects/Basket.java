import java.util.Map;
import java.util.Vector;

//Translation from Py May 10 2012
public class Basket extends EchoesObject
{
    //public classdocs
	float size = (float) 0.6;
    float [] pos = {0,0,0};   
    boolean publishRegion = true;
    boolean canBeDraged = true;        
    boolean fallTodefaultHeight = true;
    boolean falling = false;
    Vector<float[][]> shapes = new Vector<float[][]>(3,2);
    Vector<float[]> sizes = new Vector<float[]>(3,2);
    float [] worldDragOffset = {0, 0, 0};
    //Basket(autoAdd=true, props={"type" "Basket"}, fadeIn = false, fadingFrames = 100, callback = None)
    public Basket(boolean autoAdd, Map<String, String> props, boolean fadeIn, int fadingFrames, Object callback)
    {  
        super(autoAdd, props, fadeIn, fadingFrames, callback);
       
        this.defaultHeight = canvas.getRegionCoords("ground")[0][1];
               
        this.avatarTCB = null;
        
        this.stack = null;
        this.flowers = [];
        this.numflowers = 0;

        this.player = null;

        this.textures = [];
        //this.sizes = [];
        //this.shapes = [];
        this.texshape = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};        
        loadTexture("visual/images/basket-top.png");
        loadTexture("visual/images/basket-bottom.png");
        oy = (this.sizes[0][1] + this.sizes[1][1]) * 0.96;//****sizes is empty-this should give error
        w = this.sizes[0][0] / oy;
        h = 1.0-2.0*this.sizes[0][1]/oy;
        
        float [][] tempArray = {{-w, h}, {w, h}, {w, 1}, {-w, 1}};
        this.shapes.add(tempArray);
        float [][] tempArray1 = {{-w, -1}, {w, -1}, {w, h}, {-w, h}};
        h = -1.0+2.0*this.sizes[1][1]/oy;
        this.shapes.append(tempArray1);
    }
    
    public void setAttr(String item, String value)
    {
    	if (item == "pos" && hasattr("flowers"))
    	{
    		for f in this.flowers
    		{
    			f.pos[0] = value[0];
    			f.pos[1] = value[1]+f.stemLength-this.size/2;
    			f.pos[2] = value[2];
    		}
            if (hasattr("stack") && this.stack && ((hasattr("beingDragged") && this.beingDragged) || (hasattr("avatarTCB") && this.avatarTCB)))
            {    //# if (the user did it, notify the rest of the system
                split = this.stack.split();
                if (split && hasattr("beingDragged") && this.beingDragged)
                {
                	canvas.agentPublisher.agentActionCompleted("User", "unstack_basket", [str(this.id)]);
                }
            }
    	}
        else if (item == "stack")
        {
        	if (value == None)
        	{
        		canvas.rlPublisher.objectPropertyChanged(str(this.id), "basket_stack", "false");
        	}
            else
            {
            	canvas.rlPublisher.objectPropertyChanged(str(this.id), "basket_stack", "true");
            }
        }
    
        //setAttr(item, value);
    }
    public void setImage(String file)
    {
    	im = PIL.Image.open(file);// # .jpg, .bmp, etc. also work
        try
    	{
        	ix = im.size[0];
        	iy = im.size[1];
        	image = im.tostring("raw", "RGBA", 0, -1);
    	}
        catch( SystemError e)
        {
        	ix = im.size[0];
        	iy = im.size[1];
        	image = im.tostring("raw", "RGBA", 0, -1);
        }

        tex = glGenTextures(1);
        gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT,1);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
        gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, 4, ix, iy, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, image);
        
        this.textures.add(tex);
        
        float [] tempArray = {ix,iy};
        this.sizes.append(tempArray);        
    }                  
    public void renderObj(GL2 gl)
    {
    	if (! hasattr("shapes"))
    	{
    		return;
    	}
        
        if (this.numflowers != len(this.flowers))
        {
        	this.numflowers = len(this.flowers);
            canvas.rlPublisher.objectPropertyChanged(str(this.id), "basket_numflowers", str(this.numflowers));
        }   
        if (this.fallTodefaultHeight && !this.beingDragged && !this.avatarTCB)
        {
        	hdiff = this.pos[1] - this.defaultHeight;
            if (abs(hdiff) > 0.05)
            {
            	if (! this.stack)// # no stack
                {
            		this.pos[1] = this.pos[1]-hdiff/10;
                    this.falling = true;
                }
                else
                {
                	this.falling = false;
                }
            }
            else
            {
            	this.falling = false;
            }
        }
        gl.glPushMatrix();
        gl.glTranslate(this.pos[0], this.pos[1], this.pos[2]);
        gl.glScalef(this.size, this.size, this.size);
        i = 0;
        for texture in this.textures 
        {
        	gl.glEnable( GL2.GL_ALPHA_TEST );
            gl.glAlphaFunc( GL2.GL_GREATER, 0.1 );        
            gl.glEnable( GL2.GL_TEXTURE_2D );
            gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
            gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
            gl.glBindTexture(GL2.GL_TEXTURE_2D, texture);
            gl.glColor4f(1, 1, 1, this.transperancy);
            gl.glBegin(GL2.GL_QUADS);
            ti = 0;
            for(float[] v : this.shapes[i])
            {
            	gl.glTexCoord2d(this.texshape[ti][0], this.texshape[ti][1]);
                gl.glVertex3f(v[0], v[1], -0.1 + i*0.2);
                ti += 1;
            }
            gl.glEnd();
            gl.glDisable( GL2.GL_TEXTURE_2D );
            gl.glDisable( GL2.GL_ALPHA_TEST );
            i += 1;
        }
        gl.glPopMatrix();
    }          
    public void addFlower(flower)
    {
    	this.flowers.append(flower);
        flower.basket = //*****equal to what?
        flower.pos[1] = this.pos[1]+flower.stemLength-this.size/2;
        flower.inCollision = this.id;
        canvas.rlPublisher.objectPropertyChanged(str(this.id), "basket_flower", str(flower.id));
        if (flower.beingDragged)
        {
        	canvas.agentPublisher.agentActionCompleted("User", "flower_placeInBasket", [str(this.id), str(flower.id)]);
        }
    }
    
    public void growFlowers()
    {
    	if (len(this.flowers) == 0)
    	{
    		flower = objects.Plants.EchoesFlower(this.app);
            flower.size = 0.1;
            this.addFlower(flower);
    	}
        for f in this.flowers
        {
        	f.grow();
        }
    }    
    public void removeFlower(flower)
    {
    	try 
    	{
    		i = this.flowers.index(flower);
            del this.flowers[i];
    	}
        catch( ValueError e)
        {
        	Logger.warning("Basket trying to remove flower that is not in the basket, id=" + str(flower.id)); 
        }
        if (len(this.flowers) == 0)
        {
        	canvas.rlPublisher.objectPropertyChanged(str(this.id), "basket_flower", "None");
        }
    }
    
    public void playFanfare()
    {
    	if (sound.EchoesAudio.soundPresent)
    	{
    		if (!this.player) 
            {
            	//fanfar = "fanfar" + str(random.randint(1,3)) + ".wav";
            	String fanfar = "fanfar" + Integer.toString((1 + (int)(Math.random()*3))) + ".wav";
                this.player = sound.EchoesAudio.playSound(fanfar, vol=0.3);
                sound.EchoesAudio.SoundCallback(fanfar, this.resetPlayer).start();
            }
            else
            {
            	Logger.warning("Basket already plays the fanfare, not triggering new one.");
            }
    	}
    }
    
    public void resetPlayer()
    {
    	this.player = null;
    }
                       
    public void startDrag(float [] newXY)
    {
    	if (this.avatarTCB)
        {
    		this.avatarTCB.detachObject();
        }
        this.beingDragged = true;
        //# Based on http//web.iiit.ac.in/~vkrishna/data/unproj.html
        projection = glGetDoublev(GL2.GL_PROJECTION_MATRIX);
        modelview = glGetDoublev(GL2.GL_MODELVIEW_MATRIX);
        viewport = glGetIntegerv(GL2.GL_VIEWPORT);
        windowZ = glReadPixels(newXY[0], viewport[3]-newXY[1], 1, 1, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT);
        worldCoords = gluUnProject(newXY[0], viewport[3] - newXY[1], windowZ[0][0], modelview, projection, viewport);
        this.worldDragOffset[0] = this.pos[0]-worldCoords[0];
        this.worldDragOffset[1] = this.pos[1]-worldCoords[1];
        this.worldDragOffset[2] = 0;
    }   
    public void stopDrag()
    {
    	this.beingDragged = false;
    }

    public void drag(float [] newXY)
    {
    	if (this.interactive && this.canBeDraged)
    	{    //# Based on http//web.iiit.ac.in/~vkrishna/data/unproj.html
            projection = glGetDoublev(GL2.GL_PROJECTION_MATRIX);
            modelview = glGetDoublev(GL2.GL_MODELVIEW_MATRIX);
            viewport = glGetIntegerv(GL2.GL_VIEWPORT);
            windowZ = glReadPixels(newXY[0], viewport[3]-newXY[1], 1, 1, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT);
            
            worldCoords = gluUnProject(newXY[0], viewport[3] - newXY[1], windowZ[0][0], modelview, projection, viewport);
            if (this.beingDragged)
            {
            	if (this.fallTodefaultHeight)
                {
            		this.pos[0] = worldCoords[0]+this.worldDragOffset[0];
            		this.pos[1] = max(this.defaultHeight, worldCoords[1]+this.worldDragOffset[1]);
            		//this.pos[2] = this.pos[2];
                }
                else
                {
                	this.pos[0] = worldCoords[0]+this.worldDragOffset[0];
                	this.pos[1] = worldCoords[1]+this.worldDragOffset[1];                
                }
                this.locationChanged = true;
            }
    	}
    }          
    public void attachToJoint(float [] jpos, Object jori, avatarTCB)
    {
    	this.avatarTCB = avatarTCB;
        this.objectCollisionTest = false;        
        if (this.fallTodefaultHeight)
        {
        	y = max(jpos[1]+this.size/3, this.defaultHeight);
        }
        else
        {
        	y = jpos[1];
        }
        this.pos[0] = jpos[0];
        this.pos[1] = y;
    }       
    public void detachFromJoint()
    {
    	this.avatarTCB = null;
        this.objectCollisionTest = true;        
    }   
    //remove(fadeOut = false, fadingFrames = 100)
    public void remove(boolean fadeOut, int fadingFrames)
    {  
    	if (!fadeOut && this.stack && /*****something missing*/ in this.stack.pots)
     	{
    		this.objectCollisionTest = false;
     	    del this.stack.pots[this.stack.pots.index()];
            this.stack = null;
    	}
        super.remove(fadeOut, fadingFrames);
    }   
}