package net.matthiasauer.stwp4j.libgdx.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.badlogic.gdx.math.Vector2;

import net.matthiasauer.stwp4j.Channel;
import net.matthiasauer.stwp4j.ChannelInPort;
import net.matthiasauer.stwp4j.ChannelOutPort;
import net.matthiasauer.stwp4j.Scheduler;
import net.matthiasauer.stwp4j.libgdx.graphic.InputTouchEvent;
import net.matthiasauer.stwp4j.libgdx.graphic.InputTouchEventType;
import net.matthiasauer.stwp4j.libgdx.graphic.RenderData;
import net.matthiasauer.stwp4j.libgdx.graphic.RenderPositionUnit;
import net.matthiasauer.stwp4j.libgdx.graphic.SpriteRenderData;

public class TestButtonProcess {
    private SpriteRenderData createRenderData(String id, String texture) {
        SpriteRenderData data = new SpriteRenderData();

        data.set(id, 0, 0, 0, RenderPositionUnit.Percent, null, 2, true, texture);

        return data;
    }

    private void fireEvent(ChannelOutPort<InputTouchEvent> outPort, String id, InputTouchEventType type,
            boolean touched) {
        final InputTouchEvent event = new InputTouchEvent();
        event.set(type, 0, touched, new Vector2(), new Vector2());
        event.setTouchedRenderDataId(id);
        outPort.offer(event);
    }

    private void fireEvent(ChannelOutPort<InputTouchEvent> outPort, String id, InputTouchEventType type) {
        this.fireEvent(outPort, id, type, false);
    }

    private List<SpriteRenderData> performIterationAndExpect(Scheduler scheduler, ChannelInPort<RenderData> renderInput,
            String... expectedTextureName) {
        scheduler.performIteration();

        final List<String> expected = new LinkedList<String>(Arrays.asList(expectedTextureName));
        final List<SpriteRenderData> caught = new LinkedList<SpriteRenderData>();
        SpriteRenderData renderData = null;

        while ((renderData = (SpriteRenderData) renderInput.poll()) != null) {
            final String texName = renderData.getTextureName();

            if (!expected.remove(texName)) {
                fail("the expected textureName : '" + texName + "' was not found ! Only have : " + expected);
            } else {
                caught.add(renderData);
            }
        }

        assertEquals("didn't receive as many events as expected - still waiting for events ", 0, expected.size());
        
        return caught;
    }

    private void expect(Collection<SpriteRenderData> renderData, String id, String expectedTextureName) {
        String errorMessage = "";
        
        for (SpriteRenderData element : renderData) {
            if (element.getId() != null) {
                if (element.getId().equals(id) && element.getTextureName().equals(expectedTextureName)) {
                    return;
                }
                
                errorMessage += "id:'" + element.getId() + "' with texture:'" + element.getTextureName() + "', ";
            }
        }
        
        fail("found no SpriteRenderData of id '" + id + "' with texture '" + expectedTextureName + "' - only found " + errorMessage);
    }

    @Test
    public void testUntouchedGeneratesBaseState() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                true, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#1"),
                this.createRenderData("1", "tex#2"), this.createRenderData("1", "tex#3")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();

        scheduler.performIteration();

        SpriteRenderData renderData = (SpriteRenderData) renderInput.poll();

        assertNotNull("there should be a renderdata !", renderData);
        assertNull("there should be only one renderdata !", renderInput.poll());
        assertEquals("the renderData was not correct", renderData.getTextureName(), "tex#1");
    }

    @Test
    public void testRenderStatesMustBeTheSameEntity() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                true, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);

        try {
            scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                    buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#1"),
                    this.createRenderData("2", "tex#2"), this.createRenderData("3", "tex#3")));
        } catch (IllegalArgumentException e) {
            assertEquals("incorrect name", "all RenderData must have the same ID !", e.getMessage());
            return;
        } catch (Exception e) {
            fail("incorrect exception thrown : " + e);
        }

        fail("no exception thrown !");
    }

    @Test
    public void testMoveOverGeneratesOverStateContinouusly() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                false, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#1"),
                this.createRenderData("1", "tex#touched"), this.createRenderData("1", "tex#3")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();
        final ChannelOutPort<InputTouchEvent> touchOutput = touchEventChannel.createOutPort();

        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved);

        for (int i = 0; i < 10; i++) {
            this.performIterationAndExpect(scheduler, renderInput, "tex#touched");
        }

        assertNull("there should be no more renderdata !", renderInput.poll());
    }

    @Test
    public void testMoveOverGeneratesOverStateContinouuslyButNotIfOtherTarget() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                false, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#1"),
                this.createRenderData("1", "tex#touched"), this.createRenderData("1", "tex#3")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();
        final ChannelOutPort<InputTouchEvent> touchOutput = touchEventChannel.createOutPort();

        this.fireEvent(touchOutput, "12", InputTouchEventType.Moved);

        for (int i = 0; i < 10; i++) {
            this.performIterationAndExpect(scheduler, renderInput, "tex#1");
        }

        assertNull("there should be no more renderdata !", renderInput.poll());
    }

    @Test
    public void testMoveAndDownGeneratesFirstOverThenDownContinouusly() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                false, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#base"),
                this.createRenderData("1", "tex#over"), this.createRenderData("1", "tex#down")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();
        final ChannelOutPort<InputTouchEvent> touchOutput = touchEventChannel.createOutPort();

        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved);

        for (int i = 0; i < 5; i++) {
            this.performIterationAndExpect(scheduler, renderInput, "tex#over");
        }

        this.fireEvent(touchOutput, "1", InputTouchEventType.TouchDown);

        for (int i = 0; i < 5; i++) {
            this.performIterationAndExpect(scheduler, renderInput, "tex#down");
        }

        assertNull("there should be no more renderdata !", renderInput.poll());
    }

    @Test
    public void testMoveAndDownAndLeaveAndMoveAndDownAgain() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                false, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#base"),
                this.createRenderData("1", "tex#over"), this.createRenderData("1", "tex#down")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();
        final ChannelOutPort<InputTouchEvent> touchOutput = touchEventChannel.createOutPort();

        // OVER
        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved);
        this.performIterationAndExpect(scheduler, renderInput, "tex#over");

        // TOUCH
        this.fireEvent(touchOutput, "1", InputTouchEventType.TouchDown);
        this.performIterationAndExpect(scheduler, renderInput, "tex#down");

        // LEAVE
        this.fireEvent(touchOutput, "12", InputTouchEventType.Moved);
        this.performIterationAndExpect(scheduler, renderInput, "tex#base");

        // OVER
        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved);
        this.performIterationAndExpect(scheduler, renderInput, "tex#over");

        // TOUCH
        this.fireEvent(touchOutput, "1", InputTouchEventType.TouchDown);
        this.performIterationAndExpect(scheduler, renderInput, "tex#down");

        assertNull("there should be no more renderdata !", renderInput.poll());
    }

    @Test
    public void testThatInputTouchEvent() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                false, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#1"),
                this.createRenderData("1", "tex#touched"), this.createRenderData("1", "tex#3")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();
        final ChannelOutPort<InputTouchEvent> touchOutput = touchEventChannel.createOutPort();

        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved);
        this.performIterationAndExpect(scheduler, renderInput, "tex#touched");

        assertNull("there should be no more renderdata !", renderInput.poll());
    }

    @Test
    public void testButtonClickEventIsFired() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                false, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#base"),
                this.createRenderData("1", "tex#over"), this.createRenderData("1", "tex#down")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();
        final ChannelOutPort<InputTouchEvent> touchOutput = touchEventChannel.createOutPort();
        final ChannelInPort<ButtonClickEvent> buttonClickOutput = buttonClickEventChannel.createInPort();

        // OVER
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved);
        this.performIterationAndExpect(scheduler, renderInput, "tex#over");

        // TOUCH
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "1", InputTouchEventType.TouchDown);
        this.performIterationAndExpect(scheduler, renderInput, "tex#down");

        // UP
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "1", InputTouchEventType.TouchUp);
        this.performIterationAndExpect(scheduler, renderInput, "tex#over");

        ButtonClickEvent buttonClickEvent = buttonClickOutput.poll();

        assertNotNull("no buttonClickEvent generated !", buttonClickEvent);
        assertEquals("incorrect id", "1", buttonClickEvent.getId());
    }

    @Test
    public void testButtonDownStateIfMovingWhileClicked() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                false, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#base"),
                this.createRenderData("1", "tex#over"), this.createRenderData("1", "tex#down")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();
        final ChannelOutPort<InputTouchEvent> touchOutput = touchEventChannel.createOutPort();
        final ChannelInPort<ButtonClickEvent> buttonClickOutput = buttonClickEventChannel.createInPort();

        // OVER
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved);
        this.performIterationAndExpect(scheduler, renderInput, "tex#over");

        // TOUCH
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "1", InputTouchEventType.TouchDown);
        this.performIterationAndExpect(scheduler, renderInput, "tex#down");

        // OVER
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved, true);
        this.performIterationAndExpect(scheduler, renderInput, "tex#down");
    }

    @Test
    public void testCorrectHandlingOfMultipleButtonsAndMouseDown() {
        Scheduler scheduler = new Scheduler();

        final Channel<RenderData> renderChannel = scheduler.createMultiplexChannel("#1", RenderData.class, false,
                false);
        final Channel<InputTouchEvent> touchEventChannel = scheduler.createMultiplexChannel("#2", InputTouchEvent.class,
                false, false);
        final Channel<ButtonClickEvent> buttonClickEventChannel = scheduler.createMultiplexChannel("#4",
                ButtonClickEvent.class, false, true);
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("1", "tex#base"),
                this.createRenderData("1", "tex#over"), this.createRenderData("1", "tex#down")));
        scheduler.addProcess(new ButtonProcess(renderChannel.createOutPort(), touchEventChannel.createInPort(),
                buttonClickEventChannel.createOutPort(), this.createRenderData("2", "tex#base"),
                this.createRenderData("2", "tex#over"), this.createRenderData("2", "tex#down")));
        final ChannelInPort<RenderData> renderInput = renderChannel.createInPort();
        final ChannelOutPort<InputTouchEvent> touchOutput = touchEventChannel.createOutPort();
        final ChannelInPort<ButtonClickEvent> buttonClickOutput = buttonClickEventChannel.createInPort();

        // OVER
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "1", InputTouchEventType.Moved);
        List<SpriteRenderData> events1 = this.performIterationAndExpect(scheduler, renderInput, "tex#over", "tex#base");
        expect(events1, "1", "tex#over");
        expect(events1, "2", "tex#base");

        // TOUCH
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "1", InputTouchEventType.TouchDown, true);
        List<SpriteRenderData> events2 = this.performIterationAndExpect(scheduler, renderInput, "tex#down", "tex#base");
        expect(events2, "1", "tex#down");
        expect(events2, "2", "tex#base");

        // OVER
        assertNull("buttonClickEvent generated !", buttonClickOutput.poll());
        this.fireEvent(touchOutput, "2", InputTouchEventType.Moved, true);
        List<SpriteRenderData> events3 = this.performIterationAndExpect(scheduler, renderInput, "tex#down", "tex#base");
        expect(events3, "1", "tex#down");
        expect(events3, "2", "tex#base");
    }
}
